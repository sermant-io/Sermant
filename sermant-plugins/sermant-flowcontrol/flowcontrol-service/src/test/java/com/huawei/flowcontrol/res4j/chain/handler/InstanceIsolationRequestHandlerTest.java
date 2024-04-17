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
import com.huawei.flowcontrol.common.core.resolver.CircuitBreakerRuleResolver;
import com.huawei.flowcontrol.common.core.resolver.InstanceIsolationRuleResolver;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.res4j.chain.HandlerChainEntry;

import org.junit.Assert;

/**
 * instance isolation test
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class InstanceIsolationRequestHandlerTest extends CircuitRequestHandlerTest implements RequestTest {
    @Override
    public void test(HandlerChainEntry entry, String sourceName) {
        super.entry = entry;
        super.sourceName = sourceName;
        String cirMsg = getMsg();
        Assert.assertTrue(checkHttp(httpClientEntity).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboClientEntity, false).buildResponseMsg().contains(cirMsg));
        Assert.assertTrue(checkDubbo(dubboClientEntity, true).buildResponseMsg().contains(cirMsg));
    }

    @Override
    public void publishRule() {
        ResolverManager.INSTANCE.resolve(buildKey(InstanceIsolationRuleResolver.CONFIG_KEY), getRule(), false);
    }

    @Override
    public void clear() {
        ResolverManager.INSTANCE.resolve(buildKey(InstanceIsolationRuleResolver.CONFIG_KEY), getRule(), true);
    }
}
