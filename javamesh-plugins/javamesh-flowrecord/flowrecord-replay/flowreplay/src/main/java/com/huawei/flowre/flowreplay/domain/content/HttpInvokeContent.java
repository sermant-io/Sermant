/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowre.flowreplay.domain.content;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * http请求所需数据
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-02
 */
@Getter
@Setter
public class HttpInvokeContent {
    /**
     * http请求路径
     */
    String url;

    /**
     * 请求方法 GET、POST、PUT 等
     */
    String method;

    /**
     * 参数 路径参数或表单参数
     */
    Map<String, String> params;

    /**
     * http请求头
     */
    Map<String, String> headers;

    /**
     * body 数据
     */
    String data;
}
