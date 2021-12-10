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

package com.huawei.javamesh.core.lubanops.bootstrap;

public interface Interceptor extends com.huawei.javamesh.core.agent.interceptor.Interceptor {

    /**
     * 重要：该方法一般都是返回null，否则可能会影响用户业务
     *
     * @param object 拦截方法的this对象
     * @param args   拦截方法的参数
     * @return
     */
    Object[] onStart(Object object, Object[] args, String className, String methodName);

    void onError(Object object, Object[] args, Throwable e, String className, String methodName);

    void onFinally(Object object, Object[] args, Object result, String className, String methodName);

}
