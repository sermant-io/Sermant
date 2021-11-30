/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.entity;

/**
 * 执行记录明细
 *
 * @author y30010171
 * @since 2021-11-15
 **/
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