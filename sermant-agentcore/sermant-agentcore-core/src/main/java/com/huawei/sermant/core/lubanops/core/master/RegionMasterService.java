/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.core.master;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.sermant.core.lubanops.integration.transport.ClientManager;
import com.huawei.sermant.core.lubanops.core.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.sermant.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.sermant.core.lubanops.integration.transport.netty.client.NettyClientFactory;
import com.huawei.sermant.core.lubanops.integration.transport.netty.pojo.Message;
import org.apache.commons.lang3.StringUtils;

import com.huawei.sermant.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.sermant.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.core.transfer.dto.RegisterRequest;
import com.huawei.sermant.core.lubanops.core.transfer.dto.RegisterResult;
import com.huawei.sermant.core.lubanops.core.utils.HttpClientUtil;
import com.huawei.sermant.core.lubanops.core.transfer.dto.HeartBeatRequest;
import com.huawei.sermant.core.lubanops.core.transfer.dto.HeartBeatResult;

import com.huawei.sermant.core.lubanops.integration.utils.JSON;

/**
 * apm注册服务。
 * <p/>
 * apm身份信息上报、cmdb结构树存储。
 * @author
 * @author
 * @date 2020/8/17 17:13
 */
public class RegionMasterService extends AbstractMasterService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private final static String REGISTER_URL = "/sermant/master/v1/register";

    private final static String HEARTBEAT_URL = "/sermant/master/v1/heartbeat";

    /**
     * master地址列表
     */
    private List<String> masterAddressList = new ArrayList<String>();

    public RegionMasterService() {
        super(RegionMasterService.REGISTER_URL);
        if (!StringUtils.isBlank(AgentConfigManager.getMasterAddress())) {
            String[] masterAddresses = AgentConfigManager.getMasterAddress().split(",");
            List<String> masterAddress = Arrays.asList(masterAddresses);
            this.setMasterAddressList(masterAddress);
        }
    }

    // ~~ public methods

    @Override
    public int getPriority() {
        return AGENT_INTERNAL_SERVICE;
    }

    @Override
    protected RegisterResult doRegister(RegisterRequest request) {
        RegisterResult registerResult = null;
        String requestStr = JSON.toJSONString(request);
        HttpClientUtil.Result result = HttpClientUtil.sendPostToServer(masterAddressList, AgentConfigManager.getProxyList(),
                RegionMasterService.REGISTER_URL, requestStr);
        if (result != null) {
            int code = result.getStatus();
            if (code == 200) {
                RegisterResult newRegisterResult = JSON.parseObject(result.getContent(), RegisterResult.class);
                if (newRegisterResult != null && newRegisterResult.getErrorCode() == null) {
                    registerResult = newRegisterResult;
                } else if (newRegisterResult != null && newRegisterResult.getErrorCode() != null) {
                    LOGGER.log(Level.SEVERE, String.format("[APM MASTER]registry error result[%s]", newRegisterResult),
                            new ApmRuntimeException(newRegisterResult.getErrorMsg()));
                }
            } else {
                LOGGER.log(Level.SEVERE, String.format("[APM MASTER]registry error result[%s]", result));
            }
        }
        return registerResult;
    }

    @Override
    protected HeartBeatResult doHeartbeat(HeartBeatRequest heartBeatRequest) {
        HeartBeatResult newHeartBeatResult = null;
        String requestStr = JSON.toJSONString(heartBeatRequest);
        HttpClientUtil.Result result = HttpClientUtil.sendPostToServer(masterAddressList, AgentConfigManager.getProxyList(),
                RegionMasterService.HEARTBEAT_URL, requestStr);
        if (result != null) {
            int code = result.getStatus();
            if (code == 200) {
                newHeartBeatResult = JSON.parseObject(result.getContent(), HeartBeatResult.class);
            } else {
                LOGGER.log(Level.SEVERE,
                        String.format("[APM MASTER]heartbeat to master return unexpect code,raw result:[%s]",
                                result));
            }
        }
        return newHeartBeatResult;
    }

    @Override
    protected void doKafkaHeartbeat() {
        try {
            HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
            String msg = heartbeatMessage.generateCurrentMessage();
            if (msg != null && !"".equals(msg)) {
                LOGGER.log(Level.INFO, "[KafkaHeartbeatSender] heartbeat message=" + msg);
                NettyClientFactory factory = ClientManager.getNettyClientFactory();
                NettyClient nettyClient = factory.getNettyClient(
                        AgentConfigManager.getNettyServerIp(),
                        Integer.parseInt(AgentConfigManager.getNettyServerPort()));
                nettyClient.sendData(msg.getBytes(StandardCharsets.UTF_8), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
            } else {
                LOGGER.log(Level.SEVERE, "[KafkaHeartbeatSender] heartbeat json conversion error ");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[KafkaHeartbeatSender] error:" + e.toString());
        }
    }

    // ~~ container methods
    public List<String> getMasterAddressList() {
        return masterAddressList;
    }

    private void setMasterAddressList(List<String> masterAddressList) {
        this.masterAddressList = masterAddressList;
    }

}
