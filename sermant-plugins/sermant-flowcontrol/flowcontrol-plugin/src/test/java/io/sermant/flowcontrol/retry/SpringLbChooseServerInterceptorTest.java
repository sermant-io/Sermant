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

package io.sermant.flowcontrol.retry;

import static org.junit.Assert.assertNull;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.TestHelper;
import io.sermant.flowcontrol.common.core.rule.RetryRule;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;

import org.junit.Assert;
import org.junit.Test;

/**
 * spring load test
 *
 * @author zhouss
 * @since 2022-08-31
 */
public class SpringLbChooseServerInterceptorTest {
    private final Object server = new Object();

    @Test
    public void testBefore() throws Exception {
        final Interceptor interceptor = getInterceptor();
        final ExecuteContext context = interceptor.before(TestHelper.buildDefaultContext());
        assertNull(context.getResult());
        final RetryRule retryRule = new RetryRule();
        retryRule.setRetryOnSame(3);
        RetryContext.INSTANCE.buildRetryPolicy(retryRule);
        final Object instance = new Object();

        // simulated update instance
        RetryContext.INSTANCE.updateRetriedServiceInstance(instance);
        interceptor.before(context);
        assertNull(context.getResult());
        RetryContext.INSTANCE.remove();
    }

    @Test
    public void testAfter() {
        final Interceptor interceptor = getInterceptor();
        final RetryRule retryRule = new RetryRule();
        retryRule.setRetryOnSame(3);
        RetryContext.INSTANCE.buildRetryPolicy(retryRule);
        ReflectUtils.invokeMethod(interceptor, "updateServiceInstance", new Class[]{Object.class},
                new Object[]{new TestResult()});
        Assert.assertTrue(RetryContext.INSTANCE.getRetryPolicy().getAllRetriedInstance().contains(server));
        RetryContext.INSTANCE.remove();
    }

    class TestResult {
        private Object getServer() {
            return server;
        }
    }

    /**
     * get the test interceptor
     *
     * @return interceptor
     */
    protected Interceptor getInterceptor() {
        return new SpringLbChooseServerInterceptor();
    }
}
