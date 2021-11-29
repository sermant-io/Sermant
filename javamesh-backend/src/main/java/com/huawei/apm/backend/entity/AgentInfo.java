package com.huawei.apm.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentInfo {

    private Object ip;

    private Object version;

    private Object heartbeatTime;

    private String pluginsInfo;
}
