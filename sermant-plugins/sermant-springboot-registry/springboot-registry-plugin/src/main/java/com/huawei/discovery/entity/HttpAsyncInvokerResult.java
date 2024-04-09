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

/**
 * HttpAsyncClient call result, only
 * for{@link com.huawei.discovery.interceptors.httpclient.HttpAsyncClient4xInterceptor}
 *
 * @author zhouss
 * @since 2022-11-03
 */
public class HttpAsyncInvokerResult {
    private Object future;

    private Object result;

    /**
     * Invoke the result
     */
    public HttpAsyncInvokerResult() {
    }

    /**
     * Asynchronous call results
     *
     * @param future future
     * @param result The result of the call, which may be an exception
     */
    public HttpAsyncInvokerResult(Object future, Object result) {
        this.future = future;
        this.result = result;
    }

    public Object getFuture() {
        return future;
    }

    public void setFuture(Object future) {
        this.future = future;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
