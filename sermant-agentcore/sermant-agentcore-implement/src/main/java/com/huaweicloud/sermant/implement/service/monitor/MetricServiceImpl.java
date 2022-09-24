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

package com.huaweicloud.sermant.implement.service.monitor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.monitor.api.MetricService;
import com.huaweicloud.sermant.core.service.monitor.config.MonitorConfig;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * 监控插件配置服务接口实现
 *
 * @author luanwenfei
 * @since 2022-09-15
 */
public class MetricServiceImpl implements MetricService {
    private static HTTPServer httpServer = null;

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void initMonitorServer() {
        MonitorConfig monitorConfig = ConfigManager.getConfig(MonitorConfig.class);
        if (monitorConfig == null || !monitorConfig.isStartMonitor()) {
            return;
        }
        InetSocketAddress socketAddress = new InetSocketAddress(monitorConfig.getAddress(), monitorConfig.getPort());
        try {
            httpServer = new HTTPServer(socketAddress, CollectorRegistry.defaultRegistry, true);
        } catch (IOException exception) {
            LOGGER.warning("could not create httpServer for prometheus");
        }
    }

    /**
     * 启动服务
     */
    @Override
    public void start() {
        this.initMonitorServer();
    }

    /**
     * 服务关闭方法
     */
    public void stop() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}