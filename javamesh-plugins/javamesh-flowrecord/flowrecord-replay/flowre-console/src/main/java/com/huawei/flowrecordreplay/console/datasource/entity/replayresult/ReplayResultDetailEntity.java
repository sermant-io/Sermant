/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.datasource.entity.replayresult;

import com.alibaba.fastjson.JSONArray;

import lombok.Getter;
import lombok.Setter;

/**
 * 一条回放的详细比对结果及其对应接口的忽略字段结果
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-08
 */
@Getter
@Setter
public class ReplayResultDetailEntity {
    /**
     * 回放任务的id
     */
    private String jobId;

    /**
     * 回放的接口
     */
    private String method;

    /**
     * 回放请求的id
     */
    private String traceId;

    /**
     * 回放详细比对结果
     */
    private JSONArray fieldsCompare;

    /**
     * 接口字段的忽略情况
     */
    private JSONArray fieldsIgnore;
}
