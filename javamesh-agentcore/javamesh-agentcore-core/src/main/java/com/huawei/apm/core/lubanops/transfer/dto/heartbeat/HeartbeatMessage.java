package com.huawei.apm.core.lubanops.transfer.dto.heartbeat;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.bootstrap.lubanops.config.IdentityConfigManager;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.core.lubanops.utils.NetworkUtil;

import com.huawei.apm.core.ext.lubanops.utils.TimeUtil;

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
