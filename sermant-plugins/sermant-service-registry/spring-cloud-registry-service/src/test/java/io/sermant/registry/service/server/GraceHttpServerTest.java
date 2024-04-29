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

package io.sermant.registry.service.server;

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.service.impl.GraceServiceImpl;
import io.sermant.registry.service.utils.HttpClientResult;
import io.sermant.registry.service.utils.HttpClientUtils;
import io.sermant.registry.services.GraceService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Test Http Server
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class GraceHttpServerTest {
    /**
     * PluginConfigManager mock object
     */
    public MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    /**
     * PluginConfigManager mock object
     */
    public MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
    }

    /**
     * Test the effectiveness of the Http server
     */
    @Test
    public void testHttpServer() {
        final GraceConfig graceConfig = new GraceConfig();
        graceConfig.setEnableSpring(true);
        graceConfig.setEnableGraceShutdown(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(GraceService.class))
                .thenReturn(new GraceServiceImpl());
        final GraceHttpServer graceHttpServer = new GraceHttpServer();
        graceHttpServer.start();
        try {
            checkHandlers(graceHttpServer);
            final HashMap<String, Collection<String>> notifyHeaders = new HashMap<>();
            notifyHeaders
                .put(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT, Collections.singletonList("localhost:28181"));
            notifyHeaders.put(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, Collections.singletonList("test"));
            final GraceContext instance = GraceContext.INSTANCE;
            final HttpClientResult notifyResult = HttpClientUtils.INSTANCE
                .doPost("http://127.0.0.1:16688" + GraceConstants.GRACE_NOTIFY_URL_PATH,
                    JSONObject.toJSONString(new Object()), notifyHeaders);
            Assert.assertTrue(instance.getGraceShutDownManager().isMarkedOffline("localhost:28181"));
            Assert.assertEquals(notifyResult.getCode(), GraceConstants.GRACE_HTTP_SUCCESS_CODE);
        } finally {
            graceHttpServer.stop();
        }
    }

    private void checkHandlers(GraceHttpServer graceHttpServer) {
        final Optional<Object> httpServer = ReflectUtils.getFieldValue(graceHttpServer, "httpserver");
        Assert.assertTrue(httpServer.isPresent());
        final Optional<Object> server = ReflectUtils.getFieldValue(httpServer.get(), "server");
        Assert.assertTrue(server.isPresent());
        final Object serverImpl = server.get();
        final Optional<Object> contexts = ReflectUtils.getFieldValue(serverImpl, "contexts");
        Assert.assertTrue(contexts.isPresent());
        final Object contextList = contexts.get();
        final Optional<Object> list = ReflectUtils.getFieldValue(contextList, "list");
        Assert.assertTrue(list.isPresent());
        final List contextLists = (List) list.get();
        Assert.assertTrue(contextLists.size() >= 2);
    }
}
