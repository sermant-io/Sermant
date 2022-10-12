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

package com.huawei.discovery.retry;

import com.huawei.discovery.entity.Recorder;
import com.huawei.discovery.retry.Retry.RetryContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

/**
 * 默认重试测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class DefaultRetryImplTest {
    private final List<Class<? extends Throwable>> retryEx = Arrays.asList(
            ConnectException.class,
            SocketTimeoutException.class,
            NoRouteToHostException.class,
            IOException.class);

    @Test
    public void baseTest() {
        String name = "test";
        final RetryConfig retryConfig = build(name, false);
        final DefaultRetryImpl retry = new DefaultRetryImpl(retryConfig, name);
        Assert.assertEquals(retry.config(), retryConfig);
        Assert.assertEquals(retry.name(), name);
    }

    @Test
    public void contextTest() throws Exception {
        final Retry retry = Retry.create(build("context", false));
        final RetryContext<Recorder> context = retry.context();
        final Recorder recorder = Mockito.mock(Recorder.class);
        context.onBefore(recorder);
        Mockito.verify(recorder, Mockito.times(1)).beforeRequest();
        long consumeTimeMs = 100L;
        final IOException ioError = new IOException("io error");
        context.onError(recorder, ioError, consumeTimeMs);
        Mockito.verify(recorder, Mockito.times(1)).errorRequest(ioError, consumeTimeMs);
        boolean isRight = false;
        try {
            context.onError(recorder, new IllegalArgumentException("error"), consumeTimeMs);
        } catch (RetryException ex) {
            isRight = true;
        }
        Assert.assertTrue(isRight);
        final boolean result = context.onResult(recorder, new Object(), consumeTimeMs);
        Assert.assertFalse(result);
        Mockito.verify(recorder, Mockito.times(1)).afterRequest(consumeTimeMs);
        context.onComplete(recorder);
        Mockito.verify(recorder, Mockito.times(1)).completeRequest();
    }

    @Test
    public void contextResultTest() {
        final Retry retry = Retry.create(build("context", true));
        final RetryContext<Recorder> context = retry.context();
        final Recorder recorder = Mockito.mock(Recorder.class);
        final boolean result = context.onResult(recorder, new Object(), 100L);
        Assert.assertTrue(result);
    }

    private RetryConfig build(String name, boolean check) {
        return new RetryConfig(
                retryEx,
                result -> check,
                name,
                1,
                1);
    }

}
