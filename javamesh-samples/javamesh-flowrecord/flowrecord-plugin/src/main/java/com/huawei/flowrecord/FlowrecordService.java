package com.huawei.flowrecord;

import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.lubanops.config.AgentConfigManager;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.core.ext.lubanops.transport.ClientManager;
import com.huawei.apm.core.ext.lubanops.transport.netty.client.NettyClient;
import com.huawei.apm.core.ext.lubanops.transport.netty.client.NettyClientFactory;
import com.huawei.apm.core.lubanops.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.apm.core.ext.lubanops.transport.netty.pojo.Message;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class FlowrecordService implements PluginService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
            new FlowrecordThreadFactory("FLOW_RECORD_INIT_THREAD"));

    @Override
    public void init() {
        executorService.execute(new FlowRecordInitTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    static class FlowRecordInitTask implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                try {
                    // 开启定时任务（发送心跳）
                    HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
                    String msg = heartbeatMessage.registerInformation("name", "flowrecord").generateCurrentMessage();
                    if (msg != null && !"".equals(msg)) {
                        LogFactory.getLogger().log(Level.INFO, "[KafkaHeartbeatSender] heartbeat message=" + msg);
                        NettyClientFactory factory = ClientManager.getNettyClientFactory();
                        NettyClient nettyClient = factory.getNettyClient(
                                AgentConfigManager.getNettyServerIp(),
                                Integer.parseInt(AgentConfigManager.getNettyServerPort()));
                        nettyClient.sendData(msg.getBytes(StandardCharsets.UTF_8), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
                        Thread.sleep(5000);
                    } else {
                        LogFactory.getLogger().log(Level.SEVERE, "[KafkaHeartbeatSender] heartbeat json conversion error ");
                    }

                } catch (Exception e) {
                    LogFactory.getLogger().warning(String.format("Init Flow record plugin failed, {%s}", e));
                }
            }
        }
    }
}
