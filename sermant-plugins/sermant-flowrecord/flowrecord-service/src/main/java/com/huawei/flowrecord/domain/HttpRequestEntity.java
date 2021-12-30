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

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class HttpRequestEntity {
    /**
     * 请求url
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 请求头
     */
    private Map<String, String> headMap;

    /**
     * 请求json字符串
     */
    private String httpRequestBody;

    /**
     * 请求参数
     */
    private Map<String, String[]> params;
}
