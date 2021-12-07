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

package com.huawei.flowre.mockserver.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * MockClient请求体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
@Getter
@Setter
public class MockRequest {
    /**
     * 通过traceId和调用上下文获取的唯一标识key，用于准确的标识一次调用
     */
    private String subCallKey;

    /**
     * 子调用的计数
     */
    private String subCallCount;

    /**
     * 录制任务的ID
     */
    private String recordJobId;

    /**
     * 支持dubbo、http、mysql、custom类型
     */
    private String mockRequestType;

    /**
     * 请求的参数
     */
    private String arguments;

    /**
     * 接口名称
     */
    private String method;
}
