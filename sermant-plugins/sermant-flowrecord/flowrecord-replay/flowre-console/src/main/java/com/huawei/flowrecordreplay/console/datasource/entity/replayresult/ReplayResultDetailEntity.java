/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
