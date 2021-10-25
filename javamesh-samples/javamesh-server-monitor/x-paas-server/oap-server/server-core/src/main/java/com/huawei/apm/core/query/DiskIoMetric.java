/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 响应实体类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
@Setter
@Getter
public class DiskIoMetric {
    /**
     * 磁盘名称
     */
    private String diskName;

    /**
     * 磁盘对应的查询结果集合
     */
    private List<Long> valueList;

    /**
     * 指标名称（索引或者表名）
     */
    private String metricName;
}
