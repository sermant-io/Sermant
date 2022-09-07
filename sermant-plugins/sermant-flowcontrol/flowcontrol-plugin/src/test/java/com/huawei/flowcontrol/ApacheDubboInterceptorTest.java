/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol;

import com.huawei.flowcontrol.apache.ApacheInvocation;
import com.huawei.flowcontrol.apache.ApacheInvoker;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.service.rest4j.DubboRest4jService;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.monitor.support.MonitorFilter;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.cluster.directory.StaticDirectory;
import org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * apache dubbo 测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class ApacheDubboInterceptorTest {
    private ExecuteContext context;

    private Interceptor interceptor;

    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(DubboRest4jService.class))
                .thenReturn(createRestService());
        before();
    }

    public void before() {
        interceptor = new ApacheDubboInterceptor();
        Object proxy = new MonitorFilter();
        Object[] allArguments = new Object[2];
        allArguments[0] = new FailoverClusterInvoker<>(new StaticDirectory<>(URL.valueOf("dubbo://localhost:8080"),
            Collections.singletonList(new ApacheInvoker())));
        allArguments[1] = new ApacheInvocation((Invoker<?>) allArguments[0]);
        context = ExecuteContext.forMemberMethod(proxy, null, allArguments, Collections.emptyMap(),
            Collections.emptyMap());
    }

    @Test
    public void testInterceptor() throws Exception {
        interceptor.before(context);
        interceptor.after(context);
        interceptor.onThrow(context);
    }

    private DubboRest4jService createRestService() {
        return new DubboRest4jService() {

            @Override
            public void onBefore(String sourceName, RequestEntity requestEntity, FlowControlResult fixedResult,
                    boolean isProvider) {

            }

            @Override
            public void onAfter(String sourceName, Object result, boolean isProvider, boolean hasException) {

            }

            @Override
            public boolean onThrow(String sourceName, Throwable throwable, boolean isProvider) {
                return false;
            }
        };
    }
}
