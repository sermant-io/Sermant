/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.entity;

/**
 * 状态记录
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface Recorder {
    /**
     * 调用前请求
     */
    void beforeRequest();

    /**
     * 异常调用统计
     *
     * @param ex 异常类型
     * @param consumeTimeMs 调用消耗的时间
     */
    void errorRequest(Throwable ex, long consumeTimeMs);

    /**
     * 结果调用
     *
     * @param consumeTimeMs 调用消耗的时间
     */
    void afterRequest(long consumeTimeMs);

    /**
     * 结束请求
     */
    void completeRequest();
}
