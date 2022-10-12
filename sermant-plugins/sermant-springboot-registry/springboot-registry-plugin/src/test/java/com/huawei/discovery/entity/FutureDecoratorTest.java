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
import org.apache.http.ProtocolVersion;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * future修饰器测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class FutureDecoratorTest {
    @Test
    public void cancel() {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        FutureDecorator decorator = new FutureDecorator(delegate, null);
        decorator.cancel(Mockito.anyBoolean());
        Mockito.verify(delegate, Mockito.times(1)).cancel(Mockito.anyBoolean());
    }

    @Test
    public void isCancelled() {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        FutureDecorator decorator = new FutureDecorator(delegate, null);
        decorator.isCancelled();
        Mockito.verify(delegate, Mockito.times(1)).isCancelled();
    }

    @Test
    public void isDone() {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        FutureDecorator decorator = new FutureDecorator(delegate, null);
        decorator.isDone();
        Mockito.verify(delegate, Mockito.times(1)).isDone();
    }

    @Test
    public void normalGet() throws ExecutionException, InterruptedException, TimeoutException {
        normalTest();
        normalTestWithTimeout();
    }

    private void normalTestWithTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        final AtomicBoolean executed = new AtomicBoolean();
        final HttpResponse result = new ErrorCloseableHttpResponse(new Exception("wrong"),
                new ProtocolVersion("HTTP", 1, 1));
        FutureDecorator decorator = new FutureDecorator(delegate, (timeout, timeUnit) -> {
            if (timeUnit != null) {
                executed.set(true);
            }
            return result;
        });
        final HttpResponse response = decorator.get(1, TimeUnit.SECONDS);
        Assert.assertTrue(executed.get());
        Assert.assertEquals(response, result);
    }

    private void normalTest() throws ExecutionException, InterruptedException {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        final AtomicBoolean executed = new AtomicBoolean();
        final HttpResponse result = new ErrorCloseableHttpResponse(new Exception("wrong"),
                new ProtocolVersion("HTTP", 1, 1));
        FutureDecorator decorator = new FutureDecorator(delegate, (timeout, timeUnit) -> {
            executed.set(true);
            return result;
        });
        final HttpResponse response = decorator.get();
        Assert.assertTrue(executed.get());
        Assert.assertEquals(response, result);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = InterruptedException.class)
    public void errGet() throws ExecutionException, InterruptedException, TimeoutException {
        get(new InterruptedException(), false);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = InterruptedException.class)
    public void errGetTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        get(new InterruptedException(), true);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = TimeoutException.class)
    public void errGetTimeoutEx() throws ExecutionException, InterruptedException, TimeoutException {
        get(new TimeoutException(), true);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = ExecutionException.class)
    public void errGetTimeoutExecution() throws ExecutionException, InterruptedException, TimeoutException {
        get(new ExecutionException(new Exception("wrong")), true);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = ExecutionException.class)
    public void errGetExecution() throws ExecutionException, InterruptedException, TimeoutException {
        get(new ExecutionException(new Exception("wrong")), false);
    }

    /**
     * 模拟异常场景
     */
    @Test(expected = ExecutionException.class)
    public void errGetThrowable() throws ExecutionException, InterruptedException, TimeoutException {
        get(new Exception("wrong"), false);
    }

    @Test(expected = ExecutionException.class)
    public void errGetTimeoutThrowable() throws ExecutionException, InterruptedException, TimeoutException {
        get(new Exception("wrong"), false);
    }

    private void get(Throwable ex, boolean isTime) throws ExecutionException, InterruptedException, TimeoutException {
        final Future<HttpResponse> delegate = Mockito.mock(Future.class);
        final AtomicBoolean executed = new AtomicBoolean();
        FutureDecorator decorator = new FutureDecorator(delegate, (timeout, timeUnit) -> {
            executed.set(true);
            return ex;
        });
        if (isTime) {
            decorator.get(1, TimeUnit.SECONDS);
        } else {
            decorator.get();
        }
        Assert.assertTrue(executed.get());
    }
}
