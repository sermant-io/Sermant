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

package com.huawei.monitor.service.report;

import com.huawei.monitor.config.MonitorServiceConfig;
import com.huawei.monitor.service.MetricReportService;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.monitor.RegistryService;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 监控插件配置服务接口实现
 *
 * @author zhp
 * @since 2022-09-15
 */
public class PrometheusMetricServiceImpl implements MetricReportService {
    private static HTTPServer httpServer = null;

    private static final int TCP_NUM = 3;

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void startMonitorServer() {
        MonitorServiceConfig monitorServiceConfig = ConfigManager.getConfig(MonitorServiceConfig.class);
        if (StringUtils.isBlank(monitorServiceConfig.getAddress()) || monitorServiceConfig.getPort() == 0) {
            return;
        }
        InetSocketAddress socketAddress =
                new InetSocketAddress(monitorServiceConfig.getAddress(), monitorServiceConfig.getPort());
        try {
            HttpServer server = HttpServer.create(socketAddress, TCP_NUM);
            RegistryService registryService = ServiceManager.getService(RegistryService.class);
            Map<String, HttpHandler> handlerMap = registryService.getHandlers();
            if (!handlerMap.isEmpty()) {
                for (Map.Entry<String, HttpHandler> entry : handlerMap.entrySet()) {
                    server.createContext("/" + entry.getKey(), entry.getValue());
                }
            }
            httpServer = new HTTPServer(server, CollectorRegistry.defaultRegistry, true);
        } catch (IOException exception) {
            LOGGER.warning("could not create httpServer for prometheus");
        }
    }

    /**
     * 服务关闭方法
     */
    public void stopMonitorServer() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}