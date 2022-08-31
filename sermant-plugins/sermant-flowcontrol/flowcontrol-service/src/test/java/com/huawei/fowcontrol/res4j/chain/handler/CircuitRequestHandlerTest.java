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

package com.huawei.fowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.core.ResolverManager;
import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.fowcontrol.res4j.chain.HandlerChainEntry;

import org.junit.Assert;

/**
 * 熔断测试
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class CircuitRequestHandlerTest extends BaseEntityTest implements RequestTest {
    private static final int MIN_CALL = 2;
    protected HandlerChainEntry entry;
    protected String sourceName;

    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        this.entry = entry;
        this.sourceName = sourceName;
        String cirMsg = getMsg();
        Assert.assertTrue(checkHttp(httpClientEntity).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkHttp(httpServerEntity).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboClientEntity, false).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboServerEntity, false).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboClientEntity, true).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboServerEntity, true).buildResponseMsg().contains(cirMsg));
    }

    /**
     * 获取结果信息
     *
     * @return msg
     */
    protected String getMsg() {
        return "and does not permit further calls";
    }

    /**
     * 测试http
     *
     * @param requestEntity 请求体
     * @return 响应结果
     */
    protected FlowControlResult checkHttp(RequestEntity requestEntity) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        for (int i = 0; i < MIN_CALL + 1; i++) {
            entry.onBefore(sourceName, requestEntity, flowControlResult);
            entry.onThrow(sourceName, new RuntimeException("error"));
            entry.onResult(sourceName, new Object());
        }
        return flowControlResult;
    }

    /**
     * 测试dubbo
     *
     * @param requestEntity 请求体
     * @param isProvider 是否为生产端
     * @return 响应结果
     */
    protected FlowControlResult checkDubbo(RequestEntity requestEntity, boolean isProvider) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        for (int i = 0; i < MIN_CALL + 1; i++) {
            entry.onDubboBefore(sourceName, requestEntity, flowControlResult, isProvider);
            entry.onDubboThrow(sourceName, new RuntimeException("error"), isProvider);
            entry.onDubboResult(sourceName, new Object(), isProvider);
        }
        return flowControlResult;
    }

    @Override
    public void publishRule() {
        ResolverManager.INSTANCE.resolve(buildKey(CircuitBreakerRuleResolver.CONFIG_KEY), getRule(), false);
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(CircuitBreakerRuleResolver.CONFIG_KEY), getRule(), true);
    }

    /**
     * 规则
     *
     * @return 规则
     */
    protected String getRule() {
        return "failureRateThreshold: 80\n"
                + "minimumNumberOfCalls: 2\n"
                + "name: 熔断\n"
                + "slidingWindowSize: 10000\n"
                + "slidingWindowType: time\n"
                + "slowCallDurationThreshold: \"100\"\n"
                + "slowCallRateThreshold: 60\n"
                + "waitDurationInOpenState: 10s";
    }
}
