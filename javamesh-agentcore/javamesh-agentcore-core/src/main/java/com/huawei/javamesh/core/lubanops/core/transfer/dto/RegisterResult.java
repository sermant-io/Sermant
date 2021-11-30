package com.huawei.javamesh.core.lubanops.core.transfer.dto;

/**
 * 注册接口返回结果
 * @author
 */
public class RegisterResult extends Result {

    private Long appId;

    private Long envId;

    private int domainId;

    private String agentVersion;

    private Long instanceId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

}
