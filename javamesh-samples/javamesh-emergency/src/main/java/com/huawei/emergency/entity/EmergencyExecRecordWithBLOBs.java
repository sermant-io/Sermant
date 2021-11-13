package com.huawei.emergency.entity;

public class EmergencyExecRecordWithBLOBs extends EmergencyExecRecord {
    private String scriptContent;

    private String log;

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent == null ? null : scriptContent.trim();
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log == null ? null : log.trim();
    }
}