package com.huawei.apm.core.lubanops.transfer.dto;

import com.google.common.base.MoreObjects;

/**
 * 注册的请求信息
 */
public class RegisterRequest {

    private String hostName; //主机的名字

    private String mainIp;       //内部 IP

    private String instanceName;

    private String ipList;  //ip地址列表

    private String appType;

    private String businessName;

    private String subBusiness;

    private String appName;   //应用名称，必填

    private String envName;

    private String envTag; //环境标签

    private String agentVersion;

    private String source; //附属信息

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMainIp() {
        return mainIp;
    }

    public void setMainIp(String mainIp) {
        this.mainIp = mainIp;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getIpList() {
        return ipList;
    }

    public void setIpList(String ipList) {
        this.ipList = ipList;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getSubBusiness() {
        return subBusiness;
    }

    public void setSubBusiness(String subBusiness) {
        this.subBusiness = subBusiness;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getEnvTag() {
        return envTag;
    }

    public void setEnvTag(String envTag) {
        this.envTag = envTag;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("hostName", hostName)
            .add("mainIp", mainIp)
            .add("instanceName", instanceName)
            .add("ipList", ipList)
            .add("appType", appType)
            .add("businessName", businessName)
            .add("subBusiness", subBusiness)
            .add("appName", appName)
            .add("envName", envName)
            .add("envTag", envTag)
            .add("agentVersion", agentVersion)
            .add("source", source)
            .toString();
    }
}
