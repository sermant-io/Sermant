/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.interceptors.health;

import static org.junit.Assert.*;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.context.RegisterContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 测试基于心跳检测注册中心状态
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosRpcClientHealthInterceptorTest {
    private final RegisterConfig registerConfig = new RegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        RegisterContext.INSTANCE.setAvailable(true);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
        RegisterContext.INSTANCE.setAvailable(true);
    }

    @Test
    public void close() throws NoSuchMethodException {
        final NacosRpcClientHealthInterceptor interceptor = new NacosRpcClientHealthInterceptor();
        final RpcClient rpcClient = Mockito.mock(RpcClient.class);
        interceptor.doBefore(buildContext(rpcClient));
        interceptor.close();
        Mockito.verify(rpcClient, Mockito.times(1)).shutdown();
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        final NacosRpcClientHealthInterceptor interceptor = new NacosRpcClientHealthInterceptor();
        final ExecuteContext context = buildContext(new Object());
        interceptor.doAfter(context);
        Assert.assertFalse(context.isSkip());
        final ExecuteContext context1 = buildContext(new Object());
        context1.changeResult(Boolean.FALSE);
        interceptor.doAfter(context1);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
        final ExecuteContext context2 = buildContext(new Object());
        context2.changeResult(Boolean.TRUE);
        interceptor.doAfter(context2);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());
    }

    private ExecuteContext buildContext(Object target) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"), new Object[0],
                null, null);
    }

    interface RpcClient {
        void shutdown();
    }
}
