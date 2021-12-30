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

package com.huawei.flowrecord.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 流量录制入参
 *
 */
@Getter
@Setter
@Builder
public class Recorder implements Serializable {
    private String jobId;

    private String traceId;

    private String subCallKey;

    private int subCallCount;

    private String appType;

    private String methodName;

    private String requestBody;

    private String requestClass;

    private String responseBody;

    private String responseClass;

    private boolean entry;

    private Date timestamp;

    private Map<String, String> param;
}
