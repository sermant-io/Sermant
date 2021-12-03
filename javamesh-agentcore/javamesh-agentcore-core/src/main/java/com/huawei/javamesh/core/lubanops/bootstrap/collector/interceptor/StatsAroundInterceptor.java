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

package com.huawei.javamesh.core.lubanops.bootstrap.collector.interceptor;

/**
 * 无主键拦截器
 */
public interface StatsAroundInterceptor extends AroundInterceptor {
    /**
     * 在方法前执行
     *
     * @return 开始时间，单位纳秒
     */
    long onStart();

    /**
     * 在异常中执行
     *
     * @param t 异常
     */
    void onThrowable(Throwable t);

    /**
     * 在方法后执行
     *
     * @param timeInNanos 间隔时间，单位纳秒
     * @return 是否成功设置为最大耗时时间
     */
    boolean onFinally(long timeInNanos);
}
