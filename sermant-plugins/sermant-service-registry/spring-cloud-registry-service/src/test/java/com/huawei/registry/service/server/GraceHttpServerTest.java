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

package com.huawei.registry.service.server;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.service.impl.GraceServiceImpl;
import com.huawei.registry.service.utils.HttpClientResult;
import com.huawei.registry.service.utils.HttpClientUtils;
import com.huawei.registry.services.GraceService;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.alibaba.fastjson.JSONObject;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 测试Http Server
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class GraceHttpServerTest {
    /**
     * 测试HttpServer有效性
     */
    @Test
    public void testHttpServer() {
        final GraceConfig graceConfig = new GraceConfig();
        graceConfig.setEnableSpring(true);
        graceConfig.setEnableGraceShutdown(true);
        final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic
                .when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
        pluginConfigManagerMockedStatic
                .when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        Mockito.mockStatic(PluginServiceManager.class)
                .when(() -> PluginServiceManager.getPluginService(GraceService.class))
                .thenReturn(new GraceServiceImpl());
        final GraceHttpServer graceHttpServer = new GraceHttpServer();
        graceHttpServer.start();
        try{
            checkHandlers(graceHttpServer);
            final HashMap<String, String> notifyHeaders = new HashMap<>();
            notifyHeaders.put(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT, "localhost:28181");
            notifyHeaders.put(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, "test");
            final GraceContext instance = GraceContext.INSTANCE;
            final HttpClientResult notifyResult = HttpClientUtils.INSTANCE
                    .doPost("http://127.0.0.1:16688" + GraceConstants.GRACE_NOTIFY_URL_PATH,
                            JSONObject.toJSONString(new Object()),notifyHeaders);
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
