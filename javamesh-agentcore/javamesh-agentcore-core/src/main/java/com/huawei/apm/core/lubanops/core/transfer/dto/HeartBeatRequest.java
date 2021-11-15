package com.huawei.apm.core.lubanops.core.transfer.dto;

import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * @author
 * @Date
 **/
public class HeartBeatRequest {

    private Long envId; // 环境ID

    private Long instanceId; // 实例ID

    private Long appId;

    private Long businessId;

    private int domainId;

    private List<String> collectors;

    private String md5;

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public List<String> getCollectors() {
        return collectors;
    }

    public void setCollectors(List<String> collectors) {
        this.collectors = collectors;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("envId", envId)
            .add("instanceId", instanceId)
            .add("appId", appId)
            .add("businessId", businessId)
            .add("domainId", domainId)
            .add("collectors", collectors)
            .add("md5", md5)
            .toString();
    }
}
