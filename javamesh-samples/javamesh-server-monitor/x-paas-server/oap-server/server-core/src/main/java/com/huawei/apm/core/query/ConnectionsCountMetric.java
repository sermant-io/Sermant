/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 连接数指标
 *
 * @author zhouss
 * @since 2020-12-14
 **/
@Setter
@Getter
@Builder
public class ConnectionsCountMetric {
    private int activeCount;

    private int poolingCount;

    private int maxCount;
}
