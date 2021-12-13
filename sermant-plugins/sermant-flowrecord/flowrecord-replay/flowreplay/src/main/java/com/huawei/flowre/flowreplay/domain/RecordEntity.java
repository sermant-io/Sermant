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

package com.huawei.flowre.flowreplay.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 录制数据实体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-19
 */
@Getter
@Setter
public class RecordEntity {
    /**
     * 录制任务的ID
     */
    private String jobId;

    /**
     * 一条调用链的ID
     */
    private String traceId;

    /**
     * 如果是子调用，则有子调用的ID
     */
    private String subCallKey;

    /**
     * 录制的流量的类型
     */
    private String appType;

    /**
     * 录制流量的方法名
     */
    private String methodName;

    /**
     * 流量请求体
     */
    private String requestBody;

    /**
     * 流量请求体的类型
     */
    private String requestClass;

    /**
     * 流量返回数据
     */
    private String responseBody;

    /**
     * 流量返回数据的类型
     */
    private String responseClass;

    /**
     * 是否为入口调用
     */
    private boolean isEntry;

    /**
     * 录制时间
     */
    private Date timestamp;
}
