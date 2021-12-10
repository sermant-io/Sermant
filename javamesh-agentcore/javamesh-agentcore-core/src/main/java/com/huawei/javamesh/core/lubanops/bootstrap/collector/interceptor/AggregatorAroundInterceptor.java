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

package com.huawei.javamesh.core.lubanops.bootstrap.collector.interceptor;

/**
 * 单主键拦截器
 */
public interface AggregatorAroundInterceptor extends AroundInterceptor {
    /**
     * 在方法前执行
     *
     * @param key 主键
     * @return 开始时间，单位纳秒
     */
    long onStart(String key);

    /**
     * 在异常中执行
     *
     * @param t 异常
     */
    void onThrowable(Throwable t);

    /**
     * 在方法后执行
     *
     * @return 耗时间隔，单位毫秒
     */
    long onFinally();
}
