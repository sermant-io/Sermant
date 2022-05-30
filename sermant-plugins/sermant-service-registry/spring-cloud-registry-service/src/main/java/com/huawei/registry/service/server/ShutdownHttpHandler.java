/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/remoting/exchange/support/header/HeaderExchangeServer.java
 * from the Apache Dubbo project.
 */

package com.huawei.registry.service.server;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.services.GraceService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * http处理器
 *
 * @author provenceee
 * @since 2022-05-27
 */
public class ShutdownHttpHandler implements HttpHandler {
    private final GraceService graceService;

    private final GraceConfig pluginConfig;

    /**
     * 构造方法
     */
    public ShutdownHttpHandler() {
        graceService = PluginServiceManager.getPluginService(GraceService.class);
        pluginConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (GraceConstants.GRACE_HTTP_METHOD_POST.equalsIgnoreCase(exchange.getRequestMethod())) {
            OutputStream responseBody = exchange.getResponseBody();
            if (pluginConfig.isEnableGraceShutdown()) {
                graceService.shutdown();
                exchange.sendResponseHeaders(GraceConstants.GRACE_HTTP_SUCCESS_CODE,
                    GraceConstants.GRACE_OFFLINE_SUCCESS_MSG.length());
                responseBody.write(GraceConstants.GRACE_OFFLINE_SUCCESS_MSG.getBytes(StandardCharsets.UTF_8));
            } else {
                exchange.sendResponseHeaders(GraceConstants.GRACE_HTTP_FAILURE_CODE,
                    GraceConstants.GRACE_OFFLINE_SUCCESS_MSG.length());
                responseBody.write(GraceConstants.GRACE_FAILURE_MSG.getBytes(StandardCharsets.UTF_8));
            }
            responseBody.flush();
            exchange.close();
        }
    }
}
