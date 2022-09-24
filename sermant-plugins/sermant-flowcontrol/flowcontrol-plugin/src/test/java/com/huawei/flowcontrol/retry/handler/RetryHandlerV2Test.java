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

package com.huawei.flowcontrol.retry.handler;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.core.rule.RetryRule;
import com.huawei.flowcontrol.common.handler.retry.RetryContext;
import com.huawei.flowcontrol.retry.cluster.AlibabaDubboClusterInvoker.AlibabaDubboRetry;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import io.github.resilience4j.retry.Retry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 重试处理器测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class RetryHandlerV2Test {
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        operationManagerMockedStatic.close();
    }

    /**
     * 测试流程
     */
    @Test
    public void test() {
        final RetryHandlerV2 retryHandlerV2 = new RetryHandlerV2();
        final AlibabaDubboRetry alibabaDubboRetry = new AlibabaDubboRetry();
        RetryContext.INSTANCE.markRetry(alibabaDubboRetry);
        final Retry test = retryHandlerV2.createProcessor("test", new RetryRule()).get();
        Assert.assertNotNull(test);
        RetryContext.INSTANCE.remove();
    }
}
