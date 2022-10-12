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

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * producer修饰器测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpAsyncRequestProducerDecoratorTest {
    @Test
    public void getTarget() {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        AtomicBoolean isExecute = new AtomicBoolean();
        final HttpHost host = Mockito.mock(HttpHost.class);
        final Function<HttpHost, HttpHost> function = httpRequest -> {
            isExecute.set(true);
            return host;
        };
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, function);
        final HttpHost httpRequest = decorator.getTarget();
        Assert.assertEquals(httpRequest, host);
        Assert.assertTrue(isExecute.get());
    }

    @Test
    public void generateRequest() throws HttpException, IOException {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        AtomicBoolean isExecute = new AtomicBoolean();
        final HttpRequest request = Mockito.mock(HttpRequest.class);
        final Function<HttpRequest, HttpRequest> function = httpRequest -> {
            isExecute.set(true);
            return request;
        };
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, function, null);
        final HttpRequest httpRequest = decorator.generateRequest();
        Assert.assertEquals(httpRequest, request);
        Assert.assertTrue(isExecute.get());
    }

    @Test
    public void produceContent() throws IOException {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.produceContent(null, null);
        Mockito.verify(delegate, Mockito.times(1)).produceContent(null, null);
    }

    @Test
    public void requestCompleted() {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.requestCompleted(null);
        Mockito.verify(delegate, Mockito.times(1)).requestCompleted(null);
    }

    @Test
    public void failed() {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.failed(null);
        Mockito.verify(delegate, Mockito.times(1)).failed(null);
    }

    @Test
    public void isRepeatable() {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.isRepeatable();
        Mockito.verify(delegate, Mockito.times(1)).isRepeatable();
    }

    @Test
    public void resetRequest() throws IOException {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.resetRequest();
        Mockito.verify(delegate, Mockito.times(1)).resetRequest();
    }

    @Test
    public void close() throws IOException {
        final HttpAsyncRequestProducer delegate = Mockito.mock(HttpAsyncRequestProducer.class);
        final HttpAsyncRequestProducerDecorator decorator = new HttpAsyncRequestProducerDecorator(
                delegate, null, null);
        decorator.close();
        Mockito.verify(delegate, Mockito.times(1)).close();
    }
}
