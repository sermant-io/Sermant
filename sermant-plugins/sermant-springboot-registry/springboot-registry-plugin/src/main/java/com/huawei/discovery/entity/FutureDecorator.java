/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * future calls the decorator so that it can be notified to the retryer
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class FutureDecorator implements Future<HttpResponse> {
    private final BiFunction<Long, TimeUnit, HttpAsyncInvokerResult> retryFunc;

    private volatile boolean isDone = false;

    private volatile boolean isCancel = false;

    private Future<HttpResponse> delegate;

    /**
     * Retouchers
     *
     * @param retryFunc Retryer
     */
    public FutureDecorator(BiFunction<Long, TimeUnit, HttpAsyncInvokerResult> retryFunc) {
        this.retryFunc = retryFunc;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (delegate == null) {
            isCancel = true;
            return true;
        }
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        if (delegate == null) {
            return isCancel;
        }
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        if (delegate == null) {
            return isDone;
        }
        return delegate.isDone();
    }

    @Override
    public HttpResponse get() throws InterruptedException, ExecutionException {
        try {
            final Object result = process(0, null);
            if (result instanceof Throwable) {
                throw new ExecutionException((Throwable) result);
            }
            return (HttpResponse) result;
        } finally {
            isDone = true;
        }
    }

    @Override
    public HttpResponse get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            final Object result = process(timeout, unit);
            if (result instanceof TimeoutException) {
                throw (TimeoutException) result;
            } else if (result instanceof Throwable) {
                throw new ExecutionException((Throwable) result);
            }
            return (HttpResponse) result;
        } finally {
            isDone = true;
        }
    }

    private Object process(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        final HttpAsyncInvokerResult invokerResult = retryFunc.apply(timeout, unit);
        if (this.delegate == null) {
            this.delegate = (Future<HttpResponse>) invokerResult.getFuture();
        }
        final Object result = invokerResult.getResult();
        if (result instanceof InterruptedException) {
            throw (InterruptedException) result;
        } else if (result instanceof ExecutionException) {
            throw (ExecutionException) result;
        }
        return result;
    }
}
