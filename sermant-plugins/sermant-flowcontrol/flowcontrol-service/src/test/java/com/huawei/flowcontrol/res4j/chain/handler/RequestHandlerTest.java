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

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.core.ResolverManager;
import com.huawei.flowcontrol.common.core.match.MatchGroupResolver;
import com.huawei.flowcontrol.res4j.chain.HandlerChainEntry;
import com.huawei.flowcontrol.res4j.chain.context.ChainContext;
import com.huawei.flowcontrol.res4j.windows.WindowsArray;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 测试所有请求处理器（现存）
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class RequestHandlerTest {
    private HandlerChainEntry entry;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private WindowsArray windowsArray;

    private final String sourceName = this.getClass().getName();

    private final List<RequestTest> testList = new ArrayList<>();

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    /**
     * 前置处理
     */
    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        FlowControlConfig flowControlConfig = new FlowControlConfig();
        flowControlConfig.setEnableStartMonitor(true);
        flowControlConfig.setEnableSystemAdaptive(true);
        flowControlConfig.setEnableSystemRule(true);
        pluginConfigManagerMockedStatic
                .when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(flowControlConfig);
        publishMatchGroup();
        loadTests();
        entry = HandlerChainEntry.INSTANCE;
        setUpSystemRule();
    }

    private void setUpSystemRule() {
        WindowsArray.INSTANCE.initWindowsArray();
    }

    private void loadTests() {
        for (RequestTest requestTest : ServiceLoader.load(RequestTest.class)) {
            testList.add(requestTest);
        }
    }

    private void publishMatchGroup() {
        ResolverManager.INSTANCE.resolve(buildKey(MatchGroupResolver.CONFIG_KEY), getMatchGroupRule(), false);
    }

    private String buildKey(String prefix) {
        return prefix + "." + RequestTest.BUSINESS_NAME;
    }

    @After
    public void close() {
        pluginConfigManagerMockedStatic.close();
        operationManagerMockedStatic.close();
        ChainContext.remove();
    }

    /**
     * 测试限流
     */
    @Test
    public void test() {
        for(RequestTest requestTest : testList) {
            requestTest.publishRule();
            requestTest.test(entry, sourceName);
            requestTest.clear();
        }
    }

    private String getMatchGroupRule() {
        return "alias: test\n"
                + "matches:\n"
                + "- apiPath:\n"
                + "    exact: " + RequestTest.API_PATH + "\n"
                + "  headers: {}\n"
                + "  method:\n"
                + "  - POST\n"
                + "  name: degrade\n"
                + "  showAlert: false\n"
                + "  uniqIndex: c3w7x";
    }
}
