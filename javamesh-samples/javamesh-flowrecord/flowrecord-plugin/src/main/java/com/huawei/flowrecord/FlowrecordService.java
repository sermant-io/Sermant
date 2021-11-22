package com.huawei.flowrecord;

import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.heartbeat.HeartbeatService;
import com.huawei.flowrecord.config.FlowRecordConfig;
import com.huawei.flowrecord.utils.PluginConfigUtil;

public class FlowrecordService implements PluginService {

    private static final HeartbeatService HEARTBEAT_SERVICE = ServiceManager.getService(HeartbeatService.class);
    private final FlowRecordConfig flowRecordConfig = PluginConfigUtil.getFlowRecordConfig();

    @Override
    public void start() {
        HEARTBEAT_SERVICE.heartbeat(flowRecordConfig.getHeartBeatName());
    }

    @Override
    public void stop() {
        HEARTBEAT_SERVICE.stopHeartbeat(flowRecordConfig.getHeartBeatName());
    }
}
