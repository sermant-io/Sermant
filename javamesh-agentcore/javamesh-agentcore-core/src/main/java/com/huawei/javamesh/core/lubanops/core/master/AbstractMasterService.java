/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.core.master;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.huawei.javamesh.core.lubanops.bootstrap.agent.AgentInfo;
import com.huawei.javamesh.core.lubanops.bootstrap.api.TagListener;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.CollectorManager;
import com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.ConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogPathUtils;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.FileUtils;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.javamesh.core.lubanops.core.api.AgentService;
import com.huawei.javamesh.core.lubanops.core.transfer.dto.RegisterRequest;
import com.huawei.javamesh.core.lubanops.core.transfer.dto.RegisterResult;
import com.huawei.javamesh.core.lubanops.core.utils.AgentPath;
import com.huawei.javamesh.core.lubanops.core.utils.NetworkUtil;
import com.huawei.javamesh.core.lubanops.core.executor.ExecuteRepository;
import com.huawei.javamesh.core.lubanops.core.executor.timer.AbstractTimerTask;
import com.huawei.javamesh.core.lubanops.core.executor.timer.Timeout;
import com.huawei.javamesh.core.lubanops.core.monitor.HarvestTaskManager;
import com.huawei.javamesh.core.lubanops.core.transfer.TransferInvokerService;
import com.huawei.javamesh.core.lubanops.core.transfer.dto.HeartBeatRequest;
import com.huawei.javamesh.core.lubanops.core.transfer.dto.HeartBeatResult;

import com.huawei.javamesh.core.lubanops.integration.access.Address;
import com.huawei.javamesh.core.lubanops.integration.utils.JSON;

/**
 * Abstract Master Service.
 * @author
 * @date 2020/10/22 17:54
 */
public abstract class AbstractMasterService implements MasterService, ConfigService, AgentService {

    public static final String APP_TYPE_DEFAULT = "JAVA";

    /**
     * 默认心跳间隔
     */
    public final static int DEFAULT_HEARTBEAT_INTERVAL = 60;

    private static final Logger LOGGER = LogFactory.getLogger();

    private final static String SN_FILE = "sn.txt";

    private final static String CONFIG_FILE = "config.txt";

    private final Object tagAddMonitor = new Object();

    private final Object heartbeatMonitor = new Object();

    @Inject
    TransferInvokerService invokerService;

    @Inject
    HarvestTaskManager harvestTaskManager;

    TagListener tagListener;

    @Inject
    ExecuteRepository executeRepository;

    /**
     * 注册重试次数
     */
    // private int registerRetryCount = 0;
    private volatile boolean registrySucess = Boolean.FALSE;

    private String registerUrl;

    private RegisterRequest registered;

    private RegisterResult registerResult;

    private HeartBeatResult lastHeartBeatResult;

    private File snFile;

    private File configFile;

    private HeartBeatTimeTask heartBeatTimeTask;

    private volatile Timeout heartbeatTimeout;

    private volatile boolean closed = false;

    public AbstractMasterService(String registerUrl) {
        if (StringUtils.isBlank(registerUrl)) {
            throw new IllegalArgumentException("register instanceKey is blank");
        }
        this.setRegisterUrl(registerUrl);
        String snFilePath = getSnFilePath();
        snFile = new File(snFilePath);
        if (!snFile.exists() && snFile.getParentFile() != null && !snFile.getParentFile().exists()) {
            if (!snFile.getParentFile().mkdirs()) {
                throw new IllegalArgumentException(
                        "invalid master cache file " + this.snFile + "," + this.snFile.getParentFile() + ".");
            }
        }
        LOGGER.info(String.format("[APM MASTER]load local snFile[%s] success.", snFilePath));

        String configPath = getConfigFilePath();
        this.configFile = new File(configPath);
        if (!this.configFile.exists() && this.configFile.getParentFile() != null && !this.configFile.getParentFile()
                .exists()) {
            if (!this.configFile.getParentFile().mkdirs()) {
                throw new IllegalArgumentException(
                        "invalid master cache file " + this.configFile + "," + this.configFile.getParentFile() + ".");
            }
        }
        LOGGER.info(String.format("[APM MASTER]load local configFile[%s] success.", configPath));
        heartBeatTimeTask = new HeartBeatTimeTask(DEFAULT_HEARTBEAT_INTERVAL * 1000L);
    }

    @Override
    public void init() throws ApmRuntimeException {
        // heartbeat now
        tagListener = new MasterTagListener();
        this.tagListener.tagAdd(null);
        // wait register action several times
        bootstrapPark();
        LOGGER.info(String.format("[APM MASTER]start heartbeat time task[%s] success.", heartBeatTimeTask));
        LOGGER.info("[APM MASTER]registry tagListener success.");
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        registerUrl = null;
        registered = null;
        registerResult = null;
        lastHeartBeatResult = null;
        if (heartbeatTimeout != null) {
            heartbeatTimeout.cancel();
            heartbeatTimeout = null;
        }
        heartBeatTimeTask = null;
    }

    @Override
    public boolean register() {
        RegisterRequest request = buildRegistryRequest();
        LOGGER.info("[APM MASTER]register request: " + request);
        this.setRegistered(request);
        RegisterResult result = doRegister(request);
        LOGGER.info("[APM MASTER]register result: " + result);
        if (checkRegistered(result)) {
            checkRegistryResult(result);
            this.registerResult = result;
            this.registerResult.setAgentVersion(AgentInfo.getJavaagentVersion());
            FileUtils.writeFile(getSnFilePath(), JSON.toJSONString(result));
            invokerService.setNeedConnect(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int heartbeat() {
        HeartBeatRequest request = buildHeartbeatRequest();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "[APM MASTER]heartbeat request: " + request);
        }
        HeartBeatResult result = doHeartbeat(request);
        doKafkaHeartbeat();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "[APM MASTER]heartbeat result: " + result);
        }
        if (result != null && result.getErrorCode() == null && !StringUtils.isBlank(result.getMd5())) {
            if (this.lastHeartBeatResult == null || !result.getMd5().equals(this.lastHeartBeatResult.getMd5())) {
                checkHeartbeatResult(result);
                this.lastHeartBeatResult = result;
                override(this.lastHeartBeatResult);
                FileUtils.writeFile(getConfigFilePath(), JSON.toJSONString(result));
            }
            // override after first heartbeat.
            return this.lastHeartBeatResult.getHeartBeatInterval();
        } else if (result != null && "apm2.01080001".equals(result.getErrorCode())) {
            // ak/sk校验不通过
            ConfigManager.setValidated(false);
            if (lastHeartBeatResult != null) {
                lastHeartBeatResult.setMd5("0");
            }
            LOGGER.log(Level.INFO, "[APM MASTER]heartbeat to master failed,wait for next time.",
                    new ApmRuntimeException("[APM MASTER]heartbeat result: " + result));
            invokerService.dispose();
            return 0;
        } else {
            LOGGER.log(Level.INFO, "[APM MASTER]heartbeat to master failed,wait for next time.",
                    new ApmRuntimeException("[APM MASTER]heartbeat result: " + result));
            return 0;
        }

    }

    /**
     * do register with master.
     * @param request
     * @return
     */
    protected abstract RegisterResult doRegister(RegisterRequest request);

    /**
     * do heartbeat with master.
     * @param heartBeatRequest
     * @return
     */
    protected abstract HeartBeatResult doHeartbeat(HeartBeatRequest heartBeatRequest);

    /**
     * send kafka heartbeat
     */
    protected abstract void doKafkaHeartbeat();

    /**
     * get sn local store path.
     * @return
     */
    private String getSnFilePath() {
        return LogPathUtils.getLogPath() + SN_FILE;
    }

    /**
     * get config file local store path.
     * @return
     */
    private String getConfigFilePath() {
        return LogPathUtils.getLogPath() + CONFIG_FILE;
    }

    /**
     * check register if not.
     * @param heartBeatCount
     * @return
     * @throws Exception
     */
    private boolean checkRegister(int heartBeatCount) throws Exception {
        if (!registrySucess) {
            AgentConfigManager.init(AgentPath.getInstance().getAgentPath());
            if (this.register()) {
                registrySucess = true;
            }
        }
        if (registerResult != null) {
            renderIdentity();
        }
        return registrySucess;
    }

    private void renderIdentity() {
        IdentityConfigManager.setAppId(registerResult.getAppId());
        IdentityConfigManager.setBizId(registerResult.getBusinessId());
        IdentityConfigManager.setDomainId(registerResult.getDomainId());
        IdentityConfigManager.setEnvId(registerResult.getEnvId());
        IdentityConfigManager.setInstanceId(registerResult.getInstanceId());
    }

    /**
     * build heartbeat request.
     * @return
     */
    private HeartBeatRequest buildHeartbeatRequest() {
        HeartBeatRequest request = new HeartBeatRequest();
        request.setEnvId(IdentityConfigManager.getEnvId());
        request.setInstanceId(IdentityConfigManager.getInstanceId());
        request.setAppId(IdentityConfigManager.getAppId());
        request.setBusinessId(IdentityConfigManager.getBizId());
        request.setDomainId(IdentityConfigManager.getDomainId());
        request.setCollectors(CollectorManager.TAGS);
        if (lastHeartBeatResult != null
                && !org.apache.commons.lang3.StringUtils.isEmpty(lastHeartBeatResult.getMd5())) {
            request.setMd5(lastHeartBeatResult.getMd5());
        }
        return request;
    }

    /**
     * build registry request body.
     * @return
     */
    private RegisterRequest buildRegistryRequest() {
        RegisterRequest request = new RegisterRequest();
        List<String> ipList = NetworkUtil.getAllNetworkIp();
        String ipListStr = org.apache.commons.lang3.StringUtils.join(ipList, ",");
        request.setHostName(NetworkUtil.getHostName());
        request.setIpList(ipListStr);
        request.setMainIp(ipList.get(0));
        request.setInstanceName(IdentityConfigManager.getInstanceName());
        request.setAppType(APP_TYPE_DEFAULT);
        request.setBusinessName(IdentityConfigManager.getBizPath());
        request.setSubBusiness(IdentityConfigManager.getSubBusiness());
        request.setAppName(IdentityConfigManager.getAppName());
        request.setEnvName(IdentityConfigManager.getEnv());
        request.setEnvTag(IdentityConfigManager.getEnvTag());
        request.setAgentVersion(AgentInfo.getJavaagentVersion());

        return request;
    }

    private void override(HeartBeatResult heartbeatResult) {
        IdentityConfigManager.setBizId(heartbeatResult.getBusinessId());
        IdentityConfigManager.setAttachment(heartbeatResult.getAttachment());
        // global setting
        ConfigManager.setSystemProperties(heartbeatResult.getSystemProperties());
        ConfigManager.setValidated(true);
        // monitor item setting
        CollectorManager.setMonitorItemList(heartbeatResult.getMonitorItemList());
        harvestTaskManager.setMonitorConfigList(heartbeatResult.getMonitorItemList());
        // update access address list
        List<Address> accessAddressList = heartbeatResult.getAccessAddressList();
        if (accessAddressList != null && accessAddressList.size() > 0) {
            invokerService.setAccessAddressList(accessAddressList);
        }
    }

    protected void checkRegistryResult(RegisterResult result) {
        Preconditions.checkArgument(result.getAppId() > 0,
                "[APM MASTER]appId in registry result must be greater than 0.");
        Preconditions.checkArgument(result.getBusinessId() > 0,
                "[APM MASTER]businessId in registry result must be greater than 0.");
        Preconditions.checkArgument(result.getBusinessId() > 0,
                "[APM MASTER]businessId in registry result must be greater than 0.");
        Preconditions.checkArgument(result.getEnvId() > 0,
                "[APM MASTER]envId in registry result must be greater than 0.");
        Preconditions.checkArgument(result.getDomainId() > 0,
                "[APM MASTER]domainId in registry result must be greater than 0.");

    }

    protected void checkHeartbeatResult(HeartBeatResult result) {
        Preconditions.checkArgument(result.getBusinessId() != null && result.getBusinessId() > 0,
                "[APM MASTER]businessId in heartbeat result must be greater than 0.");
        Preconditions.checkArgument(!StringUtils.isBlank(result.getMd5()), "[APM MASTER]md5 in heartbeat is blank.");
        Preconditions.checkArgument(null != result.getHeartBeatInterval() && result.getHeartBeatInterval() > 0,
                "[APM MASTER]heartBeatInterval in heartbeat result can't be null and must be positive.");
        Preconditions.checkArgument(null != result.getAccessAddressList() && result.getAccessAddressList().size() > 0,
                "[APM MASTER]access address list in heartbeat result is empty,please create access point first.");
        Preconditions.checkArgument(null != result.getMonitorItemList() && result.getMonitorItemList().size() > 0,
                "[APM MASTER]monitorItemList in heartbeat result is empty.");
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public void setRegisterUrl(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public RegisterRequest getRegistered() {
        return registered;
    }

    public void setRegistered(RegisterRequest registered) {
        this.registered = registered;
    }

    /**
     * heartbeat inner class.
     */
    protected final class HeartBeatTimeTask extends AbstractTimerTask {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private final static String TASK_NAME = "HeartbeatTimeTask";

        /**
         * 记录心跳次数
         */
        private int circuitCount = 0;

        private int retryHeartbeat = 0;

        private int heartbeatCount = 0;

        private volatile boolean heartbeatSuccess = Boolean.FALSE;

        private volatile boolean hasException = Boolean.FALSE;

        public HeartBeatTimeTask(long heartbeatInterval) {
            super(heartbeatInterval);
        }

        public HeartBeatTimeTask reset() {
            HeartBeatTimeTask newTask = new HeartBeatTimeTask(getTick());
            newTask.setCircuitCount(circuitCount);
            newTask.setRetryHeartbeat(retryHeartbeat);
            newTask.setHeartbeatCount(heartbeatCount);
            return newTask;
        }

        @Override
        public String getName() {
            return TASK_NAME;
        }

        @Override
        public void doTask() {

            synchronized (heartbeatMonitor) {
                try {
                    circuitCount++;
                    heartbeatCount++;
                    if (heartbeatSuccess || needRetry()) {
                        doTry();
                    }
                } catch (Throwable e) {
                    if (heartbeatSuccess) {
                        circuitCount = 0;
                        heartbeatSuccess = false;
                        retryHeartbeat = 0;
                    }
                    ++retryHeartbeat;
                    LOGGER.log(Level.SEVERE, String.format("[APM MASTER]task[%s] has exception.", this), e);
                    this.hasException = true;
                }
            }

        }

        private void doTry() throws Exception {
            LOGGER.info(String.format("[APM MASTER]task[%s] run now[%s].", this,
                    simpleDateFormat.format(new Date())));
            if (checkRegister(0)) {
                int nextInterval = AbstractMasterService.this.heartbeat();
                if (nextInterval > 0) {
                    if (!heartbeatSuccess) {
                        heartbeatSuccess = true;
                        circuitCount = 0;
                        retryHeartbeat = 0;
                    }
                    this.setTick(nextInterval * 1000L);
                } else {
                    if (heartbeatSuccess) {
                        circuitCount = 0;
                        heartbeatSuccess = false;
                        retryHeartbeat = 0;
                    } else {
                        ++retryHeartbeat;
                    }
                }
            } else {
                if (heartbeatSuccess) {
                    circuitCount = 0;
                    heartbeatSuccess = false;
                    retryHeartbeat = 0;
                }
                ++retryHeartbeat;
            }
        }

        private boolean needRetry() {
            if (heartbeatCount <= 8) {
                return true;
            }
            int offset = circuitCount;
            boolean result = offset > 16
                    ? (offset % 16 == 0 ? true : false)
                    : (offset >= (int) Math.pow(2, retryHeartbeat) ? true : false);
            return result;
        }

        public int getCircuitCount() {
            return circuitCount;
        }

        public void setCircuitCount(int circuitCount) {
            this.circuitCount = circuitCount;
        }

        public void setRetryHeartbeat(int retryHeartbeat) {
            this.retryHeartbeat = retryHeartbeat;
        }

        public boolean isHeartbeatSuccess() {
            return heartbeatSuccess;
        }

        public void setHeartbeatSuccess(boolean heartbeatSuccess) {
            this.heartbeatSuccess = heartbeatSuccess;
        }

        public int getHeartbeatCount() {
            return heartbeatCount;
        }

        public void setHeartbeatCount(int heartbeatCount) {
            this.heartbeatCount = heartbeatCount;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("TASK_NAME", TASK_NAME)
                    .add("heartBeatCount", circuitCount)
                    .add("hasException", hasException)
                    .toString();
        }
    }

    private class MasterTagListener implements TagListener {
        @Override
        public void tagAdd(String tag) {
            AbstractMasterService.this.executeRepository.getSharedExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    synchronized (tagAddMonitor) {
                        try {
                            heartBeatTimeTask.cancel();
                            if (heartbeatTimeout != null) {
                                heartbeatTimeout.cancel();
                            }
                            heartBeatTimeTask.doTask();
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE,
                                    "[APM MASTER]heartbeat immediately failed,waiting for task executing.");
                        }
                        // reset heartbeat task
                        heartBeatTimeTask = heartBeatTimeTask.reset();
                        heartbeatTimeout = AbstractMasterService.this.executeRepository.getSharedTimer()
                                .newTimeout(heartBeatTimeTask, heartBeatTimeTask.getTick(), TimeUnit.MILLISECONDS);
                    }
                }
            });
        }

    }

    private void bootstrapPark() {
        int times = 0;
        while ((!checkRegistered(this.registerResult))
                && times < LubanApmConstants.AGENT_BOOTSTRAP_REGISTER_RETRY_TIMES) {
            try {
                Thread.sleep(LubanApmConstants.AGENT_BOOTSTRAP_REGISTER_RETRY_INTERVAL);
            } catch (InterruptedException e) {
                LogFactory.getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
            times++;
        }
    }

    private boolean checkRegistered(RegisterResult result) {
        return result != null && result.getInstanceId() > 0;
    }
}
