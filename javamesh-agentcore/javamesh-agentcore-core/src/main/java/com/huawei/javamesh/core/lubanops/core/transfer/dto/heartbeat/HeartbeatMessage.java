package com.huawei.javamesh.core.lubanops.core.transfer.dto.heartbeat;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.javamesh.core.common.BootArgsIndexer;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.core.utils.NetworkUtil;

import com.huawei.javamesh.core.lubanops.integration.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatMessage {

    private static final Logger LOGGER = LogFactory.getLogger();
    private final Map<String, Object> message = new HashMap<String, Object>();

    public HeartbeatMessage() {
        message.put("hostname", NetworkUtil.getHostName());
        message.put("ip", NetworkUtil.getAllNetworkIp());
        message.put("app", IdentityConfigManager.getAppName());
        message.put("appType", IdentityConfigManager.getAppType());
        message.put("heartbeatVersion", String.valueOf(TimeUtil.currentTimeMillis()));
        message.put("lastHeartbeat", String.valueOf(TimeUtil.currentTimeMillis()));
        message.put("version", BootArgsIndexer.getCoreVersion());
    }

    public HeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    public String generateCurrentMessage() {
        String msg = null;
        try {
            msg = JSONObject.toJSONString(message);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "[HeartbeatMessage] Generate CurrentMessage failed " + e.toString());
        }
        return msg;
    }
}
