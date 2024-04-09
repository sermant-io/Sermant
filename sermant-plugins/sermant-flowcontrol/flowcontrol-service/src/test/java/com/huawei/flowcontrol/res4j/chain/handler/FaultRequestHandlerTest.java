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

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.core.ResolverManager;
import com.huawei.flowcontrol.common.core.resolver.FaultRuleResolver;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.res4j.chain.HandlerChainEntry;

import org.junit.Assert;

/**
 * error injection testing
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class FaultRequestHandlerTest extends BaseEntityTest implements RequestTest {
    private final long sleepMs = 100L;
    private HandlerChainEntry entry;
    private String sourceName;

    /**
     * test current limiting
     */
    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        this.entry = entry;
        this.sourceName = sourceName;
        publishRule(getAbortRule());
        String abortMsg = "Request has been aborted by fault-ThrowException";
        Assert.assertEquals(checkHttp(httpClientEntity).buildResponseMsg(), abortMsg);
        Assert.assertEquals(checkDubbo(dubboClientEntity, false).buildResponseMsg(), abortMsg);
        Assert.assertEquals(checkDubbo(dubboClientEntity, true).buildResponseMsg(), abortMsg);

        publishRule(getReturnNullRule());
        String nullMsg = "Request has been aborted by fault-ReturnNull";
        Assert.assertEquals(checkHttp(httpClientEntity).getResponse().getMsg(), nullMsg);
        Assert.assertEquals(checkDubbo(dubboClientEntity, false).getResponse().getMsg(), nullMsg);
        Assert.assertEquals(checkDubbo(dubboClientEntity, true).getResponse().getMsg(), nullMsg);

        publishRule(getDelayRule());
        final long start = System.currentTimeMillis();
        checkHttp(httpClientEntity);
        Assert.assertTrue((System.currentTimeMillis() - start) > sleepMs);
    }

    private FlowControlResult checkHttp(RequestEntity requestEntity) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        entry.onBefore(sourceName, requestEntity, flowControlResult);
        entry.onThrow(sourceName, new Exception("error"));
        entry.onResult(sourceName, new Object());
        return flowControlResult;
    }

    private FlowControlResult checkDubbo(RequestEntity requestEntity, boolean isProvider) {
        final FlowControlResult flowControlResult = new FlowControlResult();
        entry.onDubboBefore(sourceName, requestEntity, flowControlResult, isProvider);
        entry.onDubboThrow(sourceName, new Exception("error"), isProvider);
        entry.onDubboResult(sourceName, new Object(), isProvider);
        return flowControlResult;
    }

    @Override
    public void publishRule() {
        publishRule(getAbortRule());
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(FaultRuleResolver.CONFIG_KEY), null, true);
    }

    public void publishRule(String rule) {
        ResolverManager.INSTANCE.resolve(buildKey(FaultRuleResolver.CONFIG_KEY), rule, false);
    }

    private String getDelayRule() {
        return "type: delay\n"
                + "percentage: 100\n"
                + "delayTime: " + sleepMs + "\n"
                + "forceClosed: false";
    }

    private String getReturnNullRule() {
        return "type: abort\n"
                + "percentage: 100\n"
                + "fallbackType: ReturnNull\n"
                + "forceClosed: false";
    }

    private String getAbortRule() {
        return "type: abort\n"
                + "percentage: 100\n"
                + "fallbackType: ThrowException\n"
                + "forceClosed: false";
    }
}
