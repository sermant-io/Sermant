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

package com.huawei.flowcontrol.retry;

import static org.junit.Assert.assertNull;

import com.huawei.flowcontrol.TestHelper;
import com.huawei.flowcontrol.common.core.rule.RetryRule;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.service.InterceptorSupporter;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * spring负载测试
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

        // 模拟更新实例
        RetryContext.INSTANCE.updateServiceInstance(instance);
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
        Assert.assertEquals(RetryContext.INSTANCE.getRetryPolicy().getLastRetryServer(), server);
        RetryContext.INSTANCE.remove();
    }

    class TestResult {
        private Object getServer() {
            return server;
        }
    }

    /**
     * 获取测试拦截器
     *
     * @return 拦截器
     */
    protected Interceptor getInterceptor() {
        return new SpringLbChooseServerInterceptor();
    }
}
