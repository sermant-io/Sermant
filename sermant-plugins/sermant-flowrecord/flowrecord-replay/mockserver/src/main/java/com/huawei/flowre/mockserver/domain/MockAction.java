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

package com.huawei.flowre.mockserver.domain;

/**
 * mock操作的枚举类 RETURN返回数据结果、SKIP跳过该方法的MOCK、THROWABLE该方法抛出异常
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
public enum MockAction {
    /**
     * 执行操作类型 返回返回数据结果
     */
    RETURN,
    /**
     * 执行操作类型 返回跳过该方法的执行
     */
    SKIP,
    /**
     * 执行操作类型 该方法抛出一个异常
     */
    THROWABLE
}
