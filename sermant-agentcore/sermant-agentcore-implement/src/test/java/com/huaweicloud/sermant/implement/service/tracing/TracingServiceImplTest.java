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

package com.huaweicloud.sermant.implement.service.tracing;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;
import com.huaweicloud.sermant.core.service.tracing.api.ExtractService;
import com.huaweicloud.sermant.core.service.tracing.api.InjectService;
import com.huaweicloud.sermant.core.service.tracing.common.SpanEvent;
import com.huaweicloud.sermant.core.service.tracing.common.TracingRequest;
import com.huaweicloud.sermant.core.utils.TracingUtils;

import com.huaweicloud.sermant.implement.service.send.netty.NettyGatewayClient;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TracingService UT
 *
 * @author luanwenfei
 * @since 2022-03-09
 */
@RunWith(MockitoJUnitRunner.class)
public class TracingServiceImplTest {
    private static final String TRACE_ID = "TRACE_ID";

    private static final String PARENT_SPAN_ID = "PARENT_SPAN_ID";

    private static final String SPAN_ID_PREFIX = "SPAN_ID_PREFIX";

    private static final String INIT_PARENT_SPAN_ID = "0";

    private static final String INIT_SPAN_ID_PREFIX = "0-0";

    private static final String INIT_SPAN_ID = "0-0-0";

    private static final String SPAN_ERROR_MESSAGE = "OnSpanError";

    private static final int STRING_SIZE = 101;

    Map<String, String> header = new HashMap<>();

    ExtractService<Map<String, String>> extractService;

    InjectService<Map<String, String>> injectService;

    /**
     * 初始化
     */
    @Before
    public void setUp() {
        header.put(TRACE_ID, "");
        header.put(PARENT_SPAN_ID, "");
        header.put(SPAN_ID_PREFIX, "");

        // 实现提取接口
        extractService = (tracingRequest, carrier) -> {
            tracingRequest.setTraceId(carrier.get(TRACE_ID));
            tracingRequest.setParentSpanId(carrier.get(PARENT_SPAN_ID));
            tracingRequest.setSpanIdPrefix(carrier.get(SPAN_ID_PREFIX));
        };

        // 实现注入接口
        injectService = (spanEvent, carrier) -> {
            carrier.put(TRACE_ID, spanEvent.getTraceId());
            carrier.put(PARENT_SPAN_ID, spanEvent.getSpanId());
            carrier.put(SPAN_ID_PREFIX, spanEvent.getNextSpanIdPrefix());
        };

    }

    /**
     * 普通span场景测试
     */
    @Test
    public void onNormalSpanStart() {
        try (MockedStatic<ServiceManager> mockedStatic = Mockito.mockStatic(ServiceManager.class)) {
            mockedStatic.when(() -> ServiceManager.getService(GatewayClient.class))
                    .thenReturn(new NettyGatewayClient());
            TracingServiceImpl tracingService = new TracingServiceImpl();

            // Service is stopped.
            tracingService.stop();
            TracingRequest tracingRequest = new TracingRequest("", "", "", "", "");
            Assert.assertFalse(tracingService.onNormalSpanStart(tracingRequest).isPresent());

            // SpanEventContext is null.
            tracingService.start();
            Assert.assertFalse(tracingService.onNormalSpanStart(tracingRequest).isPresent());

            // The normal condition.
            String traceId = TracingUtils.generateTraceId();
            header.put(TRACE_ID, traceId);
            header.put(PARENT_SPAN_ID, INIT_PARENT_SPAN_ID);
            header.put(SPAN_ID_PREFIX, INIT_SPAN_ID_PREFIX);
            tracingService.onProviderSpanStart(tracingRequest, extractService, header);
            tracingService.onSpanFinally();
            Optional<SpanEvent> spanEventOptional = tracingService.onNormalSpanStart(tracingRequest);
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertEquals(traceId, spanEventOptional.get().getTraceId());
            Assert.assertEquals("0-0-1", spanEventOptional.get().getSpanId());
            Assert.assertEquals(INIT_SPAN_ID, spanEventOptional.get().getParentSpanId());
            tracingService.onSpanFinally();

            // The second children span.
            spanEventOptional = tracingService.onNormalSpanStart(tracingRequest);
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertEquals(traceId, spanEventOptional.get().getTraceId());
            Assert.assertEquals("0-0-2", spanEventOptional.get().getSpanId());
            Assert.assertEquals(INIT_SPAN_ID, spanEventOptional.get().getParentSpanId());
            tracingService.onSpanFinally();
            tracingService.stop();
        }
    }

    /**
     * Provider场景span测试
     */
    @Test
    public void onProviderSpanStart() {
        try (MockedStatic<ServiceManager> mockedStatic = Mockito.mockStatic(ServiceManager.class)) {
            mockedStatic.when(() -> ServiceManager.getService(GatewayClient.class))
                    .thenReturn(new NettyGatewayClient());
            TracingServiceImpl tracingService = new TracingServiceImpl();

            // Service is stopped.
            tracingService.stop();
            TracingRequest tracingRequest = new TracingRequest("", "", "", "", "");
            Assert.assertFalse(tracingService.onProviderSpanStart(tracingRequest, extractService, header).isPresent());

            // SpanIdPrefix is too long.
            tracingService.start();
            String spanIdPrefix = RandomStringUtils.randomNumeric(STRING_SIZE);
            header.put(SPAN_ID_PREFIX, spanIdPrefix);
            Assert.assertFalse(tracingService.onProviderSpanStart(tracingRequest, extractService, header).isPresent());
            tracingService.stop();

            // The normal condition root span.
            tracingService.start();
            header.put(SPAN_ID_PREFIX, "");
            Optional<SpanEvent> spanEventOptional =
                    tracingService.onProviderSpanStart(tracingRequest, extractService, header);
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertFalse(spanEventOptional.get().getTraceId().isEmpty());
            Assert.assertEquals(INIT_PARENT_SPAN_ID, spanEventOptional.get().getSpanId());
            Assert.assertEquals("", spanEventOptional.get().getParentSpanId());
            tracingService.stop();

            // The normal condition children span
            tracingService.start();
            String traceId = TracingUtils.generateTraceId();
            header.put(TRACE_ID, traceId);
            header.put(PARENT_SPAN_ID, INIT_PARENT_SPAN_ID);
            header.put(SPAN_ID_PREFIX, INIT_SPAN_ID_PREFIX);
            spanEventOptional = tracingService.onProviderSpanStart(tracingRequest, extractService, header);
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertEquals(traceId, spanEventOptional.get().getTraceId());
            Assert.assertEquals(INIT_SPAN_ID, spanEventOptional.get().getSpanId());
            Assert.assertEquals(INIT_PARENT_SPAN_ID, spanEventOptional.get().getParentSpanId());
            tracingService.stop();
        }
    }

    /**
     * consumer场景span测试
     */
    @Test
    public void onConsumerSpanStart() {
        try (MockedStatic<ServiceManager> mockedStatic = Mockito.mockStatic(ServiceManager.class)) {
            mockedStatic.when(() -> ServiceManager.getService(GatewayClient.class))
                    .thenReturn(new NettyGatewayClient());
            TracingServiceImpl tracingService = new TracingServiceImpl();

            // Service is stopped.
            tracingService.stop();
            TracingRequest tracingRequest = new TracingRequest("", "", "", "", "");
            Assert.assertFalse(tracingService.onConsumerSpanStart(tracingRequest, injectService, header).isPresent());

            // SpanEventContext is null.
            tracingService.start();
            Assert.assertFalse(tracingService.onConsumerSpanStart(tracingRequest, injectService, header).isPresent());

            // The normal condition.
            String traceId = TracingUtils.generateTraceId();
            header.put(TRACE_ID, traceId);
            header.put(PARENT_SPAN_ID, INIT_PARENT_SPAN_ID);
            header.put(SPAN_ID_PREFIX, INIT_SPAN_ID_PREFIX);
            tracingService.onProviderSpanStart(tracingRequest, extractService, header);
            tracingService.onSpanFinally();
            Optional<SpanEvent> spanEventOptional =
                    tracingService.onConsumerSpanStart(tracingRequest, injectService, header);
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertEquals("0-0-1", spanEventOptional.get().getSpanId());
            Assert.assertEquals(INIT_SPAN_ID, spanEventOptional.get().getParentSpanId());
            Assert.assertEquals(traceId, header.get(TRACE_ID));
            Assert.assertEquals("0-0-1-0", header.get(SPAN_ID_PREFIX));
            tracingService.onSpanFinally();
            tracingService.stop();
        }
    }

    /**
     * span中出现错误场景测试
     */
    @Test
    public void onSpanError() {
        try (MockedStatic<ServiceManager> mockedStatic = Mockito.mockStatic(ServiceManager.class)) {
            mockedStatic.when(() -> ServiceManager.getService(GatewayClient.class))
                    .thenReturn(new NettyGatewayClient());
            TracingServiceImpl tracingService = new TracingServiceImpl();

            // Service is stopped.
            tracingService.stop();
            Assert.assertFalse(tracingService.onSpanError(new Throwable(SPAN_ERROR_MESSAGE)).isPresent());

            // SpanEventContext is null.
            tracingService.start();
            Assert.assertFalse(tracingService.onSpanError(new Throwable(SPAN_ERROR_MESSAGE)).isPresent());

            // The normal condition.
            String traceId = TracingUtils.generateTraceId();
            header.put(TRACE_ID, traceId);
            header.put(PARENT_SPAN_ID, INIT_PARENT_SPAN_ID);
            header.put(SPAN_ID_PREFIX, INIT_SPAN_ID_PREFIX);
            TracingRequest tracingRequest = new TracingRequest("", "", "", "", "");
            tracingService.onProviderSpanStart(tracingRequest, extractService, header);
            Optional<SpanEvent> spanEventOptional = tracingService.onSpanError(new Throwable(SPAN_ERROR_MESSAGE));
            Assert.assertTrue(spanEventOptional.isPresent());
            Assert.assertTrue(spanEventOptional.get().isError());
            Assert.assertEquals(SPAN_ERROR_MESSAGE, spanEventOptional.get().getErrorInfo());
            tracingService.onSpanFinally();
            tracingService.stop();
        }
    }
}