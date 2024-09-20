/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.router.spring.entity;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.function.Function;

/**
 * HttpAsyncRequestProducer modifier, handling http request
 *
 * @author zhouss
 * @since 2024-09-20
 */
public class HttpAsyncRequestProducerDecorator implements HttpAsyncRequestProducer {
    private final HttpAsyncRequestProducer httpAsyncRequestProducer;

    private final Function<HttpRequest, HttpRequest> requestDecorateFunc;

    private final Function<HttpHost, HttpHost> hostDecorateFunc;

    /**
     * Constructor
     *
     * @param httpAsyncRequestProducer Original Producer
     * @param requestDecorateFunc Request a decorator
     * @param hostDecorateFunc Address decorator
     */
    public HttpAsyncRequestProducerDecorator(
            HttpAsyncRequestProducer httpAsyncRequestProducer,
            Function<HttpRequest, HttpRequest> requestDecorateFunc,
            Function<HttpHost, HttpHost> hostDecorateFunc) {
        this.httpAsyncRequestProducer = httpAsyncRequestProducer;
        this.requestDecorateFunc = requestDecorateFunc;
        this.hostDecorateFunc = hostDecorateFunc;
    }

    @Override
    public HttpHost getTarget() {
        return hostDecorateFunc.apply(httpAsyncRequestProducer.getTarget());
    }

    @Override
    public HttpRequest generateRequest() throws IOException, HttpException {
        return requestDecorateFunc.apply(httpAsyncRequestProducer.generateRequest());
    }

    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioControl) throws IOException {
        httpAsyncRequestProducer.produceContent(encoder, ioControl);
    }

    @Override
    public void requestCompleted(HttpContext context) {
        httpAsyncRequestProducer.requestCompleted(context);
    }

    @Override
    public void failed(Exception ex) {
        httpAsyncRequestProducer.failed(ex);
    }

    @Override
    public boolean isRepeatable() {
        return httpAsyncRequestProducer.isRepeatable();
    }

    @Override
    public void resetRequest() throws IOException {
        httpAsyncRequestProducer.resetRequest();
    }

    @Override
    public void close() throws IOException {
        httpAsyncRequestProducer.close();
    }
}
