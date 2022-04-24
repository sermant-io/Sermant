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

package com.huawei.lubanops.apm.plugin.threadlocal;

import com.huaweicloud.sermant.core.agent.common.BeforeResult;
import com.huaweicloud.sermant.core.agent.interceptor.InstanceMethodInterceptor;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 线程池增强，替换Runnable和Callable参数
 *
 * @author yiwei
 * @since 2021-10-11
 */
public class ThreadPoolInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof Runnable) {
                arguments[i] = TtlRunnable.get((Runnable) arguments[i], false, true);
                continue;
            }
            if (arguments[i] instanceof Callable) {
                arguments[i] = TtlCallable.get((Callable<?>) arguments[i], false, true);
            }
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
