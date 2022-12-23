/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.intergration.flowcontrol;

import com.huaweicloud.intergration.common.FlowControlConstants;
import com.huaweicloud.intergration.common.utils.RequestUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * RestTemplate协议测试
 *
 * @author zhouss
 * @since 2022-07-30
 */
public abstract class FlowControlTest {
    @Rule
    public final TestRule flowControlCondition = new FlowControlTestRule();
    private static final int RATE_LIMITING_REQUEST_COUNT = 10;
    private static final int BREAKER_REQUEST_COUNT = 10;
    private static final String BREAKER_MSG = "is OPEN and does not permit further calls";
    private static final String RATE_LIMITING_MSG = "Rate Limited";

    /**
     * 测试服务端限流
     */
    @Test
    public void testServerRateLimiting() {
        process("/rateLimiting", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/prefixRateLimiting", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/rateLimitingContains", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/rateLimitingSuffix", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
    }

    /**
     * 测试客户端熔断-慢调用
     */
    @Test
    public void testTimedBreaker() {
        process("/timedBreaker", BREAKER_MSG, BREAKER_REQUEST_COUNT, null);
    }

    /**
     * 测试客户端熔断-异常
     */
    @Test
    public void testExceptionBreaker() {
        process("/exceptionBreaker", BREAKER_MSG, BREAKER_REQUEST_COUNT, null);
    }

    /**
     * 测试隔离仓
     */
    @Test
    public void testInstanceIsolation() {
        process("/instanceIsolation", BREAKER_MSG, BREAKER_REQUEST_COUNT, null);
    }

    /**
     * 测试隔离仓
     */
    @Test
    public void testRetry() {
        final Integer tryCount = RequestUtils.get(getRestConsumerUrl() + "/retry", Collections.emptyMap(), Integer.class);
        Assert.assertTrue(tryCount > 0);
    }

    /**
     * 测试隔离仓功能
     */
    @Test
    public void testBulkHead() throws InterruptedException {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(100));
        int cycle = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(cycle);
        final AtomicBoolean expected = new AtomicBoolean();
        for (int i = 0; i < cycle; i ++) {
            threadPoolExecutor.execute(() -> {
                try {
                    process("/bulkhead", "Bulkhead is full and does not permit further calls", RATE_LIMITING_REQUEST_COUNT, expected);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        Assert.assertTrue(expected.get());
        threadPoolExecutor.shutdown();
    }

    /**
     * 测试匹配服务名
     */
    @Test
    public void testServiceNameMatch() {
        process("/serviceNameMatch", BREAKER_MSG, RATE_LIMITING_REQUEST_COUNT, null);
    }

    /**
     * 匹配请求头测试, 见rule.yaml配置
     */
    @Test
    public void testMatchHeader() {
        process("/header", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/headerPrefix", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/headerSuffix", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/headerContains", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);
        process("/headerCompareMatch", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, null);

        final AtomicBoolean checkNotMatch = new AtomicBoolean();
        process("/headerCompareNotMatch", RATE_LIMITING_MSG, RATE_LIMITING_REQUEST_COUNT, checkNotMatch);
        if (checkNotMatch.get()) {
            Assert.fail();
        }
    }

    /**
     * 测试不匹配服务名
     */
    @Test
    public void testServiceNameNoMatch() {
        final AtomicBoolean expected = new AtomicBoolean();
        process("/serviceNameNoMatch", BREAKER_MSG, RATE_LIMITING_REQUEST_COUNT, expected);
        Assert.assertFalse(expected.get());
    }

    /**
     * 测试错误注入-返回空
     */
    @Test
    public void testFaultReturnNull() {
        final String result = RequestUtils.get(getRestConsumerUrl() + "/faultNull", Collections.emptyMap(),
                String.class);
        Assert.assertNull(result);
    }

    /**
     * 测试错误注入-抛异常
     */
    @Test
    public void testFaultThrow() {
        final String msg = RequestUtils.get(getRestConsumerUrl() + "/faultThrow", Collections.emptyMap(), String.class);
        Assert.assertTrue(msg != null && msg.contains("Request has been aborted"));
    }

    /**
     * 测试错误注入-请求延迟, 测试规则延迟2秒, 本身方法调用小于100ms
     */
    @Test
    public void testFaultDelay() {
        long start = System.currentTimeMillis();
        RequestUtils.get(getRestConsumerUrl() + "/faultDelay", Collections.emptyMap(), String.class);
        long delay = 2000L;
        Assert.assertTrue(System.currentTimeMillis() - start > delay);
    }

    private void process(String api, String flowControlMsg, int requestCount, AtomicBoolean check) {
        String url = getRestConsumerUrl() + api;
        AtomicBoolean expected = new AtomicBoolean(false);
        final BiFunction<ClientHttpResponse, String, String> callback =
                (clientHttpResponse, result) -> {
                    if (result.contains(flowControlMsg)) {
                        expected.set(true);
                        if  (check != null) {
                            check.set(true);
                        }
                    }
                    return result;
                };
        for (int i = 0; i < requestCount; i++) {
            if (expected.get()) {
                break;
            }
            try {
                RequestUtils.get(url, Collections.emptyMap(), String.class, callback);
            } catch (Exception ex) {
                if (ex.getMessage().startsWith(FlowControlConstants.COMMON_FLOW_CONTROL_CODE)) {
                    expected.set(true);
                    if  (check != null) {
                        check.set(true);
                    }
                }
            }
        }
        if (check == null) {
            Assert.assertTrue(expected.get());
        }
    }

    /**
     * 获取请求地址
     *
     * @return 地址
     */
    protected abstract String getRestConsumerUrl();
}
