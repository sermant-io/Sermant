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

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.retry.cluster.AlibabaDubboCluster;
import com.huawei.flowcontrol.retry.cluster.ApacheDubboCluster;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.apache.dubbo.rpc.cluster.Cluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * test inject cluster
 *
 * @author zhouss
 * @since 2022-08-31
 */
public class ExtensionLoaderInterceptorTest {
    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
    }

    @Test
    public void testNoExecute() throws NoSuchMethodException {
        final ExtensionLoaderInterceptor interceptor = new ExtensionLoaderInterceptor();
        final HashMap<String, Class<?>> result = new HashMap<>();
        interceptor.doAfter(buildContext(null, result));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testApacheDubbo() throws NoSuchMethodException {
        final ExtensionLoaderInterceptor interceptor = new ExtensionLoaderInterceptor();
        final HashMap<String, Class<?>> result = new HashMap<>();
        interceptor.doAfter(buildContext(true, result));
        Assert.assertEquals(result.get(flowControlConfig.getRetryClusterInvoker()), ApacheDubboCluster.class);
    }

    @Test
    public void testAlibabaDubbo() throws NoSuchMethodException {
        final ExtensionLoaderInterceptor interceptor = new ExtensionLoaderInterceptor();
        final HashMap<String, Class<?>> result = new HashMap<>();
        interceptor.doAfter(buildContext(false, result));
        Assert.assertEquals(result.get(flowControlConfig.getRetryClusterInvoker()), AlibabaDubboCluster.class);
    }

    private ExecuteContext buildContext(Boolean isApache, Map<String, Class<?>> result) throws NoSuchMethodException {
        Object obj;
        if (isApache == null) {
            obj = new NoExecuteTest();
        } else if (isApache) {
            obj = new ApacheDubboTest();
        } else {
            obj = new AlibabaDubboTest();
        }
        final ExecuteContext context = ExecuteContext.forMemberMethod(obj, String.class.getDeclaredMethod(
                        "trim"), null, null, null);
        context.changeResult(result);
        return context;
    }

    static class ApacheDubboTest {
        private final Class<?> type = Cluster.class;
    }

    static class AlibabaDubboTest {
        private final Class<?> type = com.alibaba.dubbo.rpc.cluster.Cluster.class;
    }

    static class NoExecuteTest {
        private final Class<?> type = String.class;
    }
}
