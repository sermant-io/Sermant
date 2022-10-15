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

import org.apache.http.HttpResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

/**
 * future调用修饰器, 使之可通知到重试器
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class FutureDecorator implements Future<HttpResponse> {
    private final Future<HttpResponse> delegate;
    private final BiFunction<Long, TimeUnit, Object> retryFunc;

    /**
     * 修饰器
     *
     * @param delegate 代理
     * @param retryFunc 重试器
     */
    public FutureDecorator(Future<HttpResponse> delegate,
            BiFunction<Long, TimeUnit, Object> retryFunc) {
        this.delegate = delegate;
        this.retryFunc = retryFunc;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public HttpResponse get() throws InterruptedException, ExecutionException {
        final Object result = process(0, null);
        if (result instanceof Throwable) {
            throw new ExecutionException((Throwable) result);
        }
        return (HttpResponse) result;
    }

    @Override
    public HttpResponse get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final Object result = process(timeout, unit);
        if (result instanceof TimeoutException) {
            throw (TimeoutException) result;
        } else if (result instanceof Throwable) {
            throw new ExecutionException((Throwable) result);
        }
        return (HttpResponse) result;
    }

    private Object process(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        final Object result = retryFunc.apply(timeout, unit);
        if (result instanceof InterruptedException) {
            throw (InterruptedException) result;
        } else if (result instanceof ExecutionException) {
            throw (ExecutionException) result;
        }
        return result;
    }
}
