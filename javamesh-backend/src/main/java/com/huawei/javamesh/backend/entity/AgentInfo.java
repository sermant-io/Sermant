package com.huawei.javamesh.backend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentInfo {

    private Object ip;

    private Object version;

    private Object lastHeartbeatTime;

    private Object heartbeatTime;

    private List<PluginInfo> pluginsInfos;
}
