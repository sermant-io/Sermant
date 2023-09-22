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

package com.huaweicloud.sermant.router.config.handler;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.Strategy;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试EnabledStrategyHandler
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class EnabledStrategyHandlerTest {
    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private final AbstractConfigHandler handler;

    /**
     * 初始化
     */
    @BeforeClass
    public static void init() {
        RouterConfig config = new RouterConfig();
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class)).thenReturn(config);
    }

    /**
     * 清除mock
     */
    @AfterClass
    public static void clear() {
        mockPluginConfigManager.close();
    }

    public EnabledStrategyHandlerTest() {
        this.handler = new EnabledStrategyHandler();
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWithOrderConfigEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("strategy", "white");
        data.put("value", "service-a,service-b");
        String content = "strategy: white\nvalue: service-a,service-b";
        DynamicConfigEvent event = new OrderConfigEvent("sermant.plugin.router", "foo", content,
            DynamicConfigEventType.CREATE, data);
        handler.handle(event, "testHandleWithOrderConfigEvent");
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWithOrderConfigEvent");
        Assert.assertEquals(Strategy.WHITE, strategy.getStrategy());
        Assert.assertEquals(2, strategy.getValue().size());
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWithDynamicConfigEvent() {
        String content = "strategy: all";
        DynamicConfigEvent event = new DynamicConfigEvent("sermant.plugin.router", "foo", content,
            DynamicConfigEventType.CREATE);
        handler.handle(event, "testHandleWithDynamicConfigEvent");
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWithDynamicConfigEvent");
        Assert.assertEquals(Strategy.ALL, strategy.getStrategy());
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWhenDelete() {
        String content = "strategy: all";
        DynamicConfigEvent event = new DynamicConfigEvent("sermant.plugin.router", "foo", content,
            DynamicConfigEventType.DELETE);
        handler.handle(event, "testHandleWhenDelete");
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWhenDelete");
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWithNullStrategy() {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWithNullStrategy");
        strategy.reset(Strategy.NONE, Collections.emptyList());
        String content = "strategy: null";
        DynamicConfigEvent event = new DynamicConfigEvent("sermant.plugin.router", "foo", content,
            DynamicConfigEventType.MODIFY);
        handler.handle(event, "testHandleWithNullStrategy");
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWithInvalidStrategy() {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWithInvalidStrategy");
        strategy.reset(Strategy.NONE, Collections.emptyList());
        String content = "strategy: invalid";
        DynamicConfigEvent event = new DynamicConfigEvent("sermant.plugin.router", "foo", content,
            DynamicConfigEventType.MODIFY);
        handler.handle(event, "testHandleWithInvalidStrategy");
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandleWithNullContent() {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy("testHandleWithNullContent");
        strategy.reset(Strategy.NONE, Collections.emptyList());
        DynamicConfigEvent event = new DynamicConfigEvent("sermant.plugin.router", "foo", null,
            DynamicConfigEventType.MODIFY);
        handler.handle(event, "testHandleWithNullContent");
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());
    }

    /**
     * 测试shouldHandle方法
     */
    @Test
    public void testShouldHandle() {
        Assert.assertTrue(handler.shouldHandle("sermant.plugin.router"));
        Assert.assertFalse(handler.shouldHandle("sermant.plugin.foo"));
    }
}