/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 录制任务请求
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Getter
@Setter
public class QueryReplayJobRequest {
    /**
     * 任务名称
     */
    private String name;

    /**
     * 应用名称
     */
    private String application;
}
