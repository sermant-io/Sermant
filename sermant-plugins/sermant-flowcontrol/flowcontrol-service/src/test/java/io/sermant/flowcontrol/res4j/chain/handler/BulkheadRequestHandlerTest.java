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

package io.sermant.flowcontrol.res4j.chain.handler;

import io.sermant.flowcontrol.common.core.ResolverManager;
import io.sermant.flowcontrol.common.core.resolver.BulkheadRuleResolver;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.res4j.chain.HandlerChainEntry;

import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BulkheadRequestHandlerTest
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class BulkheadRequestHandlerTest extends BaseEntityTest implements RequestTest {
    private HandlerChainEntry entry;
    private String sourceName;

    /**
     * test current limiting
     */
    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        this.entry = entry;
        this.sourceName = sourceName;
        final CountDownLatch countDownLatch = new CountDownLatch(300);
        AtomicBoolean check = new AtomicBoolean();
        final Thread[] threads = createThread(10, countDownLatch, check);
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // ignored
        } finally {
            Assert.assertTrue(check.get());
        }
    }

    private Thread[] createThread(int num, CountDownLatch countDownLatch, AtomicBoolean check) {
        final ThreadGroup test = new ThreadGroup("test");
        final Thread[] threads = new Thread[num];
        for (int i = 0; i < num; i++) {
            threads[i] = new Thread(test, () -> {
                execute(countDownLatch, check);
            }, "thread-" + i);
        }
        return threads;
    }

    private void execute(CountDownLatch countDownLatch, AtomicBoolean check) {
        while (countDownLatch.getCount() > 0) {
            if (executeHttp(httpClientEntity)) {
                check.set(true);
            }
            countDownLatch.countDown();
        }
    }

    private boolean executeHttp(RequestEntity requestEntity) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        final Object result = new Object();
        entry.onBefore(sourceName, requestEntity, flowControlResult);
        entry.onResult(sourceName, result);
        return flowControlResult.buildResponseMsg().contains("Bulkhead is full and does not permit further calls");
    }

    @Override
    public void publishRule() {
        ResolverManager.INSTANCE.resolve(buildKey(BulkheadRuleResolver.CONFIG_KEY), getRule(), false);
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(BulkheadRuleResolver.CONFIG_KEY), null, true);
    }

    private String getRule() {
        return "maxConcurrentCalls: \"1\"\n"
                + "maxWaitDuration: 1\n"
                + "name: testBulkhead";
    }
}
