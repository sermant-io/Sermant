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

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.util.ConvertUtils;
import com.huawei.flowcontrol.retry.cluster.AlibabaDubboClusterInvoker;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * alibaba dubbo重试逻辑测试
 *
 * @author zhouss
 * @since 2022-08-31
 */
public class AlibabaDubboInvokerInterceptorTest {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
        operationManagerMockedStatic.close();
    }

    /**
     * 前置初始化
     *
     * @throws Exception 初始化失败抛出
     */
    @Before
    @Ignore
    public void before() throws Exception {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
    }

    @Test
    public void test() throws NoSuchMethodException {
        final AlibabaDubboInvokerInterceptor interceptor = new AlibabaDubboInvokerInterceptor();
        final ExecuteContext executeContext = buildContext();
        interceptor.doBefore(executeContext);
        interceptor.doAfter(executeContext);
        Assert.assertTrue(executeContext.getResult() instanceof RpcResult);
        Assert.assertEquals(((RpcResult) executeContext.getResult()).getValue(), getResult());
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        final Invocation invocation = Mockito.mock(Invocation.class);
        String interfaceName = this.getClass().getName();
        String version = "1.0.0";
        Mockito.when(invocation.getMethodName()).thenReturn("test");
        Mockito.when(invocation.getAttachment(ConvertUtils.DUBBO_ATTACHMENT_VERSION)).thenReturn(version);
        Mockito.when(invocation.getArguments()).thenReturn(new Object[]{"test"});
        final Invoker invoker = Mockito.mock(Invoker.class);
        Mockito.when(invoker.getInterface()).thenReturn(this.getClass());
        final URL url = Mockito.mock(URL.class);
        Mockito.when(url.getParameter(CommonConst.GENERIC_INTERFACE_KEY, interfaceName)).thenReturn(interfaceName);
        Mockito.when(url.getParameter(CommonConst.URL_VERSION_KEY, version)).thenReturn(version);
        Mockito.when(url.getParameter(CommonConst.DUBBO_REMOTE_APPLICATION)).thenReturn("application");
        Mockito.when(invoker.getUrl()).thenReturn(url);
        Mockito.when(invocation.getInvoker()).thenReturn(invoker);
        final LoadBalance loadBalance = Mockito.mock(LoadBalance.class);
        final Directory directory = Mockito.mock(Directory.class);
        Mockito.when(directory.getUrl()).thenReturn(url);
        Mockito.when(invoker.invoke(invocation)).thenReturn(new RpcResult(getResult()));
        final AlibabaDubboClusterInvoker clusterInvoker = new AlibabaDubboClusterInvoker<>(directory);
        return ExecuteContext.forMemberMethod(clusterInvoker,
                this.getClass().getDeclaredMethod("getResult"),
                new Object[] {invocation, Collections.singletonList(invoker), loadBalance},
                Collections.emptyMap(), Collections.emptyMap());
    }

    private String getResult() {
        return "result";
    }

}
