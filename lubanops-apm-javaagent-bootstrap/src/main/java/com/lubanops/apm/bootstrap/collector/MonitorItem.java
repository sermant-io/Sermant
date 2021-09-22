package com.lubanops.apm.bootstrap.collector;

import java.util.Map;

/**
 * 监控项
 */
public class MonitorItem {
    private String collectorName;

    private Integer interval;

    private Integer collectorId;

    private Long monitorItemId;

    private Integer status;

    private Map<String, String> parameters;

    public String getCollectorName() {
        return collectorName;
    }

    public void setCollectorName(String collectorName) {
        this.collectorName = collectorName;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Integer collectorId) {
        this.collectorId = collectorId;
    }

    public Long getMonitorItemId() {
        return monitorItemId;
    }

    public void setMonitorItemId(Long monitorItemId) {
        this.monitorItemId = monitorItemId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitorItem{");
        sb.append("collectorName='").append(collectorName).append('\'');
        sb.append(", interval=").append(interval);
        sb.append(", collectorId=").append(collectorId);
        sb.append(", monitorItemId=").append(monitorItemId);
        sb.append(", status=").append(status);
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }
}