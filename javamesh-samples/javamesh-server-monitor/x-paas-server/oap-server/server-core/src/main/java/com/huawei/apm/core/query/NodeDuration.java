/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询时间段
 *
 * @author zhouss
 * @since 2020-11-29
 */
@Getter
@Setter
@Builder
public class NodeDuration {
    /**
     * 开始时间
     */
    private long start;

    /**
     * 结束时间
     */
    private long end;
}
