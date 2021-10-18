package com.huawei.apm.bootstrap.lubanops.collector.api;

import java.util.List;

public class MetricSet {
    private String name;

    private int code;

    private String msg;

    private String attachment;

    private List<MonitorDataRow> dataRows;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public List<MonitorDataRow> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<MonitorDataRow> dataRows) {
        this.dataRows = dataRows;
    }

}