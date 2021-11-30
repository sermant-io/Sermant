package com.huawei.emergency.entity;

import java.util.Date;

public class EmergencyExecRecordDetail {
    private Integer detailId;

    private Integer execId;

    private Integer recordId;

    private String status;

    private String serverIp;

    private String createUser;

    private Date createTime;

    private Date startTime;

    private Date endTime;

    private String ensureUser;

    private Date ensureTime;

    private String isValid;

    private Integer pid;

    private Integer serverId;

    private String log;

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public Integer getExecId() {
        return execId;
    }

    public void setExecId(Integer execId) {
        this.execId = execId;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp == null ? null : serverIp.trim();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEnsureUser() {
        return ensureUser;
    }

    public void setEnsureUser(String ensureUser) {
        this.ensureUser = ensureUser == null ? null : ensureUser.trim();
    }

    public Date getEnsureTime() {
        return ensureTime;
    }

    public void setEnsureTime(Date ensureTime) {
        this.ensureTime = ensureTime;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid == null ? null : isValid.trim();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log == null ? null : log.trim();
    }
}