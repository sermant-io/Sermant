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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import com.google.common.base.Optional;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.TestHelper;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.common.core.rule.RetryRule;
import io.sermant.flowcontrol.common.handler.retry.RetryContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * ribbon test
 *
 * @author zhouss
 * @since 2022-08-31
 */
public class SpringRibbonChooseServerInterceptorTest {
    private final Object server = new Object();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    /**
     * pre initialization
     */
    @Before
    public void before() throws Exception {
        XdsFlowControlConfig xdsFlowControlConfig = new XdsFlowControlConfig();
        xdsFlowControlConfig.setEnable(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(()->PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class))
                .thenReturn(xdsFlowControlConfig);
    }

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
        List<Object> instances = new ArrayList<>();
        instances.add(instance);
        instances.add(new Object());
        context.changeArgs(new Object[]{instances});
        interceptor.before(context);
        instances = (List<Object>) context.getArguments()[0];
        assertFalse(instances.contains(instance));
        RetryContext.INSTANCE.remove();
    }

    @Test
    public void testAfter() throws NoSuchMethodException {
        final Interceptor interceptor = getInterceptor();
        final RetryRule retryRule = new RetryRule();
        retryRule.setRetryOnSame(3);
        RetryContext.INSTANCE.buildRetryPolicy(retryRule);
        final ExecuteContext context = TestHelper.buildDefaultContext();
        context.changeResult(Optional.of(server));
        ReflectUtils.invokeMethod(interceptor, "updateServiceInstance", new Class[]{context.getClass()},
                new Object[]{context});
        Assert.assertTrue(RetryContext.INSTANCE.getRetryPolicy().getAllRetriedInstance().contains(server));
        List<Object> serverList = new ArrayList<>();
        serverList.add(server);
        Object instance = new Object();
        serverList.add(instance);
        context.changeArgs(new Object[]{serverList});
        ReflectUtils.invokeMethod(interceptor, "removeRetriedServiceInstance", new Class[]{context.getClass()},
                new Object[]{context});
        Assert.assertEquals(((List<?>)context.getArguments()[0]).get(0), instance);
        RetryContext.INSTANCE.remove();
    }

    private Interceptor getInterceptor() {
        return new SpringRibbonChooseServerInterceptor();
    }

    @After
    public void after() {
        pluginConfigManagerMockedStatic.close();
    }
}
