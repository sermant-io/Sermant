/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.query.enumeration.Step;

/**
 * disk查询的条件
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
@Setter
@Getter
public class DiskQueryCondition {
    /**
     * 实例名
     */
    private String serviceInstanceName;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 指标名称（索引或者表名）
     */
    private String metricName;

    /**
     * 索引中应该有的时间数组
     */
    private String[] timeArr;
}
