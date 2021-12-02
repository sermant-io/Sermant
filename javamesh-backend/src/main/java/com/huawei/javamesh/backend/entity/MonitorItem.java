package com.huawei.javamesh.backend.entity;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorItem {
    private String collectorName;

    private Integer interval;

    private Integer collectorId;

    private Long monitorItemId;

    private Integer status;

    private Map<String, String> parameters;
}
