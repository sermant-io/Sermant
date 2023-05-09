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

package com.huawei.flowcontrol.res4j.chain;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.entity.DubboRequestEntity;
import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity.Builder;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.res4j.chain.context.ChainContext;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.config.EventConfig;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 链调用入口测试
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class HandlerChainEntryTest {
    private final HandlerChainEntry instance = HandlerChainEntry.INSTANCE;

    private final String sourceName = this.getClass().getName();

    private final RequestEntity httpEntity = new Builder()
            .setApiPath("/test")
            .setMethod("POST")
            .setRequestType(RequestType.SERVER)
            .build();

    private final RequestEntity dubboEntity = new DubboRequestEntity("/test", Collections.emptyMap(),
            RequestType.CLIENT, "application");

    private final FlowControlResult flowControlResult = new FlowControlResult();

    private final Object methodResult = new Object();

    private static EventConfig config;

    private static MockedStatic<ConfigManager> mockConfigManager;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    /**
     * mock配置
     */
    @Before
    public void setUp() {
        config = new EventConfig();
        config.setEnable(false);
        mockConfigManager = Mockito.mockStatic(ConfigManager.class);
        mockConfigManager.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(config);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic
                .when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class))
                .thenReturn(new FlowControlConfig());
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    /**
     * 关闭mock
     */
    @After
    public void close() {
        pluginConfigManagerMockedStatic.close();
        operationManagerMockedStatic.close();
        mockConfigManager.close();
        ChainContext.remove();
    }

    /**
     * 测试前置调用
     */
    @Test
    public void onBefore() {
        instance.onBefore(sourceName, httpEntity, flowControlResult);
    }

    /**
     * 测试dubbo前置调用
     */
    @Test
    public void onDubboBefore() {
        instance.onDubboBefore(sourceName, dubboEntity, flowControlResult, false);
        instance.onDubboBefore(sourceName, dubboEntity, flowControlResult, true);
    }

    /**
     * 测试响应调用
     */
    @Test
    public void onResult() {
        instance.onResult(sourceName, methodResult);
    }

    /**
     * 测试dubbo响应调用
     */
    @Test
    public void onDubboResult() {
        instance.onDubboResult(sourceName, methodResult, false);
        instance.onDubboResult(sourceName, methodResult, true);
    }

    /**
     * 测试异常调用
     */
    @Test
    public void onThrow() {
        instance.onThrow(sourceName, new Exception("error"));
    }

    /**
     * 测试dubbo异常调用
     */
    @Test
    public void onDubboThrow() {
        instance.onDubboThrow(sourceName, new Exception("error"), false);
        instance.onDubboThrow(sourceName, new Exception("error"), true);
    }
}
