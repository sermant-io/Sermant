package com.huawei.apm.core.lubanops.core.master;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.integration.transport.ClientManager;
import com.huawei.apm.core.lubanops.core.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.apm.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.apm.core.lubanops.integration.transport.netty.client.NettyClientFactory;
import com.huawei.apm.core.lubanops.integration.transport.netty.pojo.Message;
import org.apache.commons.lang3.StringUtils;

import com.huawei.apm.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.apm.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.core.transfer.dto.RegisterRequest;
import com.huawei.apm.core.lubanops.core.transfer.dto.RegisterResult;
import com.huawei.apm.core.lubanops.core.utils.HttpClientUtil;
import com.huawei.apm.core.lubanops.core.transfer.dto.HeartBeatRequest;
import com.huawei.apm.core.lubanops.core.transfer.dto.HeartBeatResult;

import com.huawei.apm.core.lubanops.integration.utils.JSON;

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

    private final static String REGISTER_URL = "/apm2/master/v1/register";

    private final static String HEARTBEAT_URL = "/apm2/master/v1/heartbeat";

    /**
     * master地址列表
     */
    private List<String> masterAddressList = new ArrayList<String>();

    public RegionMasterService() {
        super(RegionMasterService.REGISTER_URL);
        if (!StringUtils.isBlank(AgentConfigManager.getMasterAddress())) {
            String[] masterAddresses = AgentConfigManager.getMasterAddress().split(",");
            List<String> masterAddressList = Arrays.asList(masterAddresses);
            this.setMasterAddressList(masterAddressList);
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
