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

import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 反注册状态判断测试
 *
 * @author zhouss
 * @since 2022-12-20
 */
public class NacosGrpcDeRegisterInterceptorTest {
    private final RegisterConfig registerConfig = new RegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() throws Exception {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void test() throws NoSuchMethodException {
        final NacosGrpcDeRegisterInterceptor interceptor = new NacosGrpcDeRegisterInterceptor();
        final ExecuteContext context = buildContext(new NacosGrpcProxy1());
        final ExecuteContext context1 = interceptor.doBefore(context);
        Assert.assertFalse(context1.isSkip());
        final ExecuteContext context2 = buildContext(new NacosRealGrpcProxy());
        final ExecuteContext context3 = interceptor.doBefore(context2);
        Assert.assertFalse(context3.isSkip());
        final NacosRealGrpcProxy nacosRealGrpcProxy = new NacosRealGrpcProxy();
        nacosRealGrpcProxy.rpcClient.isShutdown = true;
        final ExecuteContext context4 = buildContext(nacosRealGrpcProxy);
        final ExecuteContext context5 = interceptor.doBefore(context4);
        Assert.assertTrue(context5.isSkip());
    }

    private ExecuteContext buildContext(Object target) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"), new Object[0],
                null, null);
    }

    static class NacosGrpcProxy1 {

    }

    static class NacosRealGrpcProxy {
        RpcClient rpcClient = new RpcClient();
    }

    static class RpcClient {
        boolean isShutdown = false;

        boolean isShutdown() {
            return isShutdown;
        }
    }
}
