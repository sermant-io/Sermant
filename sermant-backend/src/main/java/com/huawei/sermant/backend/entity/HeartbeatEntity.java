package com.huawei.sermant.backend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 心跳请求数据
 *
 * @author xuezechao
 * @since 2022-02-28
 */

@Getter
@Setter
public class HeartbeatEntity {

    private String app;

    private String hostname;

    private long heartbeatVersion;

    private String pluginVersion;

    private long lastHeartbeat;

    private String pluginName;

    private String appType;

    private List<String> ip;

    private String version;

    private String instanceId;
}
