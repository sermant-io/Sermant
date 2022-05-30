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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginService;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * http server
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class GraceHttpServer implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final GraceConfig pluginConfig;

    private HttpServer httpserver;

    /**
     * 构造方法
     */
    public GraceHttpServer() {
        pluginConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    /**
     * 服务启动方法
     */
    @Override
    public void start() {
        if (!pluginConfig.isEnableSpring() || !pluginConfig.isEnableGraceShutdown()) {
            return;
        }
        HttpServerProvider provider = HttpServerProvider.provider();
        try {
            httpserver = provider.createHttpServer(new InetSocketAddress(pluginConfig.getHttpServerPort()), 0);
            httpserver.createContext(GraceConstants.GRACE_NOTIFY_URL_PATH, new NotifyHttpHandler());
            httpserver.createContext(GraceConstants.GRACE_SHUTDOWN_URL_PATH, new ShutdownHttpHandler());
            httpserver.start();
            LOGGER.info("HttpServer startup successfully...");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "HttpServer startup failure...", e);
        }
    }

    /**
     * 服务关闭方法
     */
    @Override
    public void stop() {
        if (httpserver != null) {
            httpserver.stop(1);
        }
    }
}
