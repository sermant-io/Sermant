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

package com.huawei.flowre.mockserver.config;

/**
 * Mock Server 字符串常量
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-05
 */
public class MSConst {
    /**
     * 配置跳过的method列表的zk节点
     */
    public static final String SKIP_METHOD = "/mock_method/";

    /**
     * 子调用index前缀
     */
    public static final String SUB_CALL_RECORD_PREFIX = "subcall_";

    /**
     * 子调用查找的keyword
     */
    public static final String SUB_CALL_KEY = "subCallKey";

    /**
     * 子调用计数
     */
    public static final String SUB_CALL_COUNT = "subCallCount";

    /**
     * 录制应用类型
     */
    public static final String APP_TYPE = "appType";

    /**
     * 录制请求体
     */
    public static final String REQUEST_BODY = "requestBody";

    /**
     * 录制返回值类型
     */
    public static final String RESPONSE_CLASS = "responseClass";

    /**
     * 录制返回值
     */
    public static final String RESPONSE_BODY = "responseBody";

    /**
     * Dubbo
     */
    public static final String DUBBO = "Dubbo";

    /**
     * Redisson
     */
    public static final String REDISSON = "Redisson";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 冒号
     */
    public static final String COLON = ":";


}
