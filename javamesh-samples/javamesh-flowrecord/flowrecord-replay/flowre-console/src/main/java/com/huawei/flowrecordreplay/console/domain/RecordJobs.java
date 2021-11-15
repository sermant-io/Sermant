/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.domain;

import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 录制任务列表
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-29
 */
@Getter
@Setter
public class RecordJobs {
    private List<RecordJobEntity> jobs;

    private long total;
}
