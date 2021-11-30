/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.recordresult;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 录制流量总接口统计
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-06-10
 */
@Getter
@Setter
public class RecordResultCountEntity {
    /**
     * 录制任务的id
     */
    private String jobId;

    /**
     * 录制总数目
     */
    private long recordTotalCount;

    /**
     * 录制接口统计
     */
    private List<RecordInterfaceCountEntity> recordInterfaceCount;
}
