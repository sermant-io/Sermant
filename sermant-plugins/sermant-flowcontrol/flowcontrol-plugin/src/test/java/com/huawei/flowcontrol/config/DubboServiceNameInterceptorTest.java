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

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.FlowControlServiceMeta;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * test gets dubbo service name
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class DubboServiceNameInterceptorTest {
    private final String serviceName = "test";
    private final FlowControlConfig flowControlConfig = new FlowControlConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Before
    public void setUp() {
        flowControlConfig.setUseCseRule(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
    }

    /**
     * the test gets the service name
     *
     * @throws Exception will not throw
     */
    @Test
    public void test() throws Exception {
        final DubboServiceNameInterceptor interceptor = new DubboServiceNameInterceptor();
        final ExecuteContext executeContext = buildContext();
        interceptor.before(executeContext);
        Assert.assertEquals(FlowControlServiceMeta.getInstance().getServiceName(), serviceName);
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(
                this,
                String.class.getMethod("trim"),
                new Object[]{serviceName},
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }
}
