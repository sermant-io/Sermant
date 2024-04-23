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

package io.sermant.monitor.service.report;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginService;
import io.sermant.core.utils.StringUtils;
import io.sermant.monitor.common.MetricReportType;
import io.sermant.monitor.config.MonitorServiceConfig;
import io.sermant.monitor.service.MetricReportService;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of monitoring plugin configuration service interface
 *
 * @author luanwenfei
 * @since 2022-09-15
 */
public class MetricServiceImpl implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static MetricReportService metricReportService;

    @Override
    public void start() {
        LOGGER.info("start monitor service");
        MonitorServiceConfig config = PluginConfigManager.getPluginConfig(MonitorServiceConfig.class);
        if (config == null || StringUtils.isEmpty(config.getReportType()) || !config.isEnableStartService()) {
            LOGGER.info("monitor switch is close, not start monitor");
            return;
        }
        Optional<MetricReportType> optional = MetricReportType.getMetricReportType(config.getReportType());
        if (optional.isPresent()) {
            metricReportService = optional.get().getMetricReportService();
            metricReportService.startMonitorServer();
        }
    }

    @Override
    public void stop() {
        if (metricReportService != null) {
            metricReportService.stopMonitorServer();
        }
    }
}