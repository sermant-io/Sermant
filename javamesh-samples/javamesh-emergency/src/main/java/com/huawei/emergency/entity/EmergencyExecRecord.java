package com.huawei.emergency.entity;

import java.util.Date;

public class EmergencyExecRecord {
    private Integer recordId;

    private Integer execId;

    private Integer planId;

    private Integer sceneId;

    private Integer taskId;

    private Integer preSceneId;

    private Integer preTaskId;

    private Integer parentTaskId;

    private String status;

    private Integer scriptId;

    private String scriptName;

    private String scriptType;

    private String scriptParams;

    private String serverIp;

    private String serverUser;

    private String havePassword;

    private String passwordMode;

    private String password;

    private String createUser;

    private Date createTime;

    private Date startTime;

    private Date endTime;

    private String ensureUser;

    private String isValid;

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getExecId() {
        return execId;
    }

    public void setExecId(Integer execId) {
        this.execId = execId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getPreSceneId() {
        return preSceneId;
    }

    public void setPreSceneId(Integer preSceneId) {
        this.preSceneId = preSceneId;
    }

    public Integer getPreTaskId() {
        return preTaskId;
    }

    public void setPreTaskId(Integer preTaskId) {
        this.preTaskId = preTaskId;
    }

    public Integer getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Integer getScriptId() {
        return scriptId;
    }

    public void setScriptId(Integer scriptId) {
        this.scriptId = scriptId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName == null ? null : scriptName.trim();
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType == null ? null : scriptType.trim();
    }

    public String getScriptParams() {
        return scriptParams;
    }

    public void setScriptParams(String scriptParams) {
        this.scriptParams = scriptParams == null ? null : scriptParams.trim();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp == null ? null : serverIp.trim();
    }

    public String getServerUser() {
        return serverUser;
    }

    public void setServerUser(String serverUser) {
        this.serverUser = serverUser == null ? null : serverUser.trim();
    }

    public String getHavePassword() {
        return havePassword;
    }

    public void setHavePassword(String havePassword) {
        this.havePassword = havePassword == null ? null : havePassword.trim();
    }

    public String getPasswordMode() {
        return passwordMode;
    }

    public void setPasswordMode(String passwordMode) {
        this.passwordMode = passwordMode == null ? null : passwordMode.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
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

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid == null ? null : isValid.trim();
    }

    private Integer detailId;

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }
}