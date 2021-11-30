package com.huawei.javamesh.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResult extends Result {

    private Long appId;

    private Long envId;

    private int domainId;

    private String agentVersion;

    private Long instanceId;
}

