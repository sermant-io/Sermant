package com.huawei.flowrecord;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatService;
import com.huawei.flowrecord.config.FlowRecordConfig;
import com.huawei.flowrecord.utils.PluginConfigUtil;

import java.util.HashMap;
import java.util.Map;

public class FlowrecordService implements PluginService {

    private static final HeartbeatService HEARTBEAT_SERVICE = CoreServiceManager.INSTANCE.getService(HeartbeatService.class);
    private final FlowRecordConfig flowRecordConfig = PluginConfigUtil.getFlowRecordConfig();

    @Override
    public void init() {
        HEARTBEAT_SERVICE.heartbeat(flowRecordConfig.getHeartBeatName());
    }

    @Override
    public void stop() {
        HEARTBEAT_SERVICE.stopHeartbeat(flowRecordConfig.getHeartBeatName());
    }
}
