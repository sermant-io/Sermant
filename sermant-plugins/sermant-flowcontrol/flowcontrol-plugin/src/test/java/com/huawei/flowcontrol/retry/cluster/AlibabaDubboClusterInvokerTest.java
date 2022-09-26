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

package com.huawei.flowcontrol.retry.cluster;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.util.ConvertUtils;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.loadbalance.RoundRobinLoadBalance;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * alibaba dubbo invoker 测试
 *
 * @author zhouss
 * @since 2022-09-14
 */
public class AlibabaDubboClusterInvokerTest {
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
    public void before() throws Exception {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new YamlConverterImpl());
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
    }

    @Test
    @Ignore
    public void doInvoke() {
        final Directory<Result> directory = Mockito.mock(Directory.class);
        Mockito.when(directory.getUrl()).thenReturn(URL.valueOf("dubbo://localhost:8080"));
        final AlibabaDubboClusterInvoker<Result> clusterInvoker = new AlibabaDubboClusterInvoker<>(directory);
        final RoundRobinLoadBalance roundRobinLoadBalance = new RoundRobinLoadBalance();
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
        Mockito.when(directory.getUrl()).thenReturn(url);
        final RpcResult rpcResult = new RpcResult(new Object());
        Mockito.when(invoker.invoke(invocation)).thenReturn(rpcResult);
        final Result result = clusterInvoker.doInvoke(invocation, Arrays.asList(invoker), roundRobinLoadBalance);
        Assert.assertEquals(result, rpcResult);
        // 测试抛出异常
        Mockito.when(invoker.invoke(invocation)).thenThrow(new RpcException("test error"));
        boolean isEx = false;
        try {
            clusterInvoker.doInvoke(invocation, Arrays.asList(invoker), roundRobinLoadBalance);
        } catch (RpcException ex) {
            isEx = true;
        }
        Assert.assertTrue(isEx);
    }
}
