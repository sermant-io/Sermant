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

package io.sermant.flowcontrol.res4j.chain;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.common.config.XdsFlowControlConfig;
import io.sermant.flowcontrol.res4j.service.ServiceCollectorService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

/**
 * HandlerChainBuilderTest
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class HandlerChainBuilderTest {
    private static final String FIELD_NAME = "HANDLERS";

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(XdsFlowControlConfig.class))
                .thenReturn(new XdsFlowControlConfig());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * ChainBuilder，determine the number of handlers
     */
    @Test
    public void testBuild() {
        final HandlerChain build = HandlerChainBuilder.INSTANCE.build();
        AbstractChainHandler next = build.getNext();
        int minHandlerNum = 4;
        while (next.getNext() != null) {
            next = next.getNext();
            minHandlerNum--;
        }
        Assert.assertTrue(minHandlerNum < 0);
    }

    /**
     * gets the built handler instance object
     */
    @Test
    public void testGetHandler() {
        Optional<AbstractChainHandler> notExistOptional =
                HandlerChainBuilder.getHandler(ServiceCollectorService.class.getName());
        Assert.assertNotNull(notExistOptional);
        Assert.assertFalse(notExistOptional.isPresent());
    }

    /**
     * tests static method execution
     */
    @Test
    public void testStatic() {
        Optional<Object> optional = ReflectUtils.getStaticFieldValue(HandlerChainBuilder.class, FIELD_NAME);
        Assert.assertNotNull(optional);
        Assert.assertTrue(optional.isPresent());
        boolean castFlag = optional.get() instanceof List;
        Assert.assertTrue(castFlag);
        List<?> handlerList = (List<?>) optional.get();
        Assert.assertTrue(handlerList.size() > 0);
    }
}
