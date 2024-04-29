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
import io.sermant.flowcontrol.common.core.resolver.RateLimitingRuleResolver;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.RequestEntity;
import io.sermant.flowcontrol.res4j.chain.HandlerChainEntry;

import org.junit.Assert;

/**
 * rate limiting test
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class RateLimitingRequestHandlerTest extends BaseEntityTest implements RequestTest {
    private HandlerChainEntry entry;
    private String sourceName;

    /**
     * test rate limiting
     */
    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        this.entry = entry;
        this.sourceName = sourceName;
        checkHttpRateLimiting(httpClientEntity);
        checkHttpRateLimiting(httpServerEntity);
        checkDubboRateLimiting(dubboServerEntity, false);
        checkDubboRateLimiting(dubboServerEntity, true);
        checkDubboRateLimiting(dubboClientEntity, false);
        checkDubboRateLimiting(dubboClientEntity, true);
    }

    @Override
    public void publishRule() {
        ResolverManager.INSTANCE.resolve(buildKey(RateLimitingRuleResolver.CONFIG_KEY), getRule(), false);
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(RateLimitingRuleResolver.CONFIG_KEY), null, true);
    }

    private String getRule() {
        return "limitRefreshPeriod: \"1000\"\n"
                + "name: flow\n"
                + "rate: \"1\"";
    }

    private void checkDubboRateLimiting(RequestEntity requestEntity, boolean isProvider) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        final Object result = new Object();
        final Exception exception = new Exception("error");
        for (int i = 0; i < 2; i++) {
            entry.onDubboBefore(sourceName, requestEntity, flowControlResult, isProvider);
            entry.onDubboThrow(sourceName, exception, isProvider);
            entry.onDubboResult(sourceName, result, isProvider);
        }
        Assert.assertEquals(flowControlResult.buildResponseMsg(), "Rate Limited");
    }
    private void checkHttpRateLimiting(RequestEntity requestEntity) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        final Object result = new Object();
        final Exception exception = new Exception("error");
        for (int i = 0; i < 2; i++) {
            entry.onBefore(sourceName, requestEntity, flowControlResult);
            entry.onThrow(sourceName, exception);
            entry.onResult(sourceName, result);
        }
        Assert.assertEquals(flowControlResult.buildResponseMsg(), "Rate Limited");
    }
}
