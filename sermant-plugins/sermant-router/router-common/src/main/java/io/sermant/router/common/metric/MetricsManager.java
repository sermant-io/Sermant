/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.router.common.metric;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.metric.api.Counter;
import io.sermant.core.service.metric.api.MetricService;
import io.sermant.core.service.metric.api.Tags;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.cache.AppCache;
import io.sermant.router.common.cache.DubboCache;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Routing Metrics Management Class
 *
 * @author zhp
 * @since 2024-10-16
 */
public class MetricsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<MetricInfo, Counter> COUNT_MAP = new ConcurrentHashMap<>();

    private static final RouterConfig ROUTER_CONFIG = PluginConfigManager.getPluginConfig(RouterConfig.class);

    private static final Map<String, String> TAG_KEY_MAP = new ConcurrentHashMap<>();

    private static MetricService metricService = null;

    static {
        try {
            metricService = ServiceManager.getService(MetricService.class);
            TAG_KEY_MAP.put("service", "service_meta_service");
            TAG_KEY_MAP.put("version", "service_meta_version");
            TAG_KEY_MAP.put("application", "service_meta_application");
            TAG_KEY_MAP.put("zone", "service_meta_zone");
            TAG_KEY_MAP.put("project", "service_meta_project");
            TAG_KEY_MAP.put("environment", "service_meta_environment");
            TAG_KEY_MAP.put("parameters", "service_meta_parameters");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Failed to load metrics service", e);
        }
    }

    /**
     * Constructor
     */
    private MetricsManager() {
    }

    /**
     * Add or update the value of the metric.
     *
     * @param metricName metric Name
     * @param tags tag information
     * @param value count add value
     */
    public static void addOrUpdateCounterMetricValue(String metricName, Map<String, String> tags, double value) {
        if (metricService == null || !ROUTER_CONFIG.isEnableMetric()) {
            return;
        }
        Map<String, String> tagsMap;
        if (tags != null) {
            tagsMap = new HashMap<>(tags);
        } else {
            tagsMap = new HashMap<>();
        }
        tagsMap.put(RouterConstant.SCOPE, "service-router");
        Counter counter = COUNT_MAP.computeIfAbsent(new MetricInfo(metricName, tagsMap),
                metricInfo -> metricService.counter(metricName, Tags.of(tagsMap)));
        counter.increment(value);
    }

    /**
     * Collect request count metric information
     *
     * @param url url information
     */
    public static void collectRequestCountMetric(URL url) {
        String address = url.getHost() + RouterConstant.URL_CONNECTOR + url.getPort();
        collectRequestCountMetric(address);
    }

    /**
     * Collect request count metric information
     *
     * @param uri uri information
     */
    public static void collectRequestCountMetric(URI uri) {
        String address = uri.getHost() + RouterConstant.URL_CONNECTOR + uri.getPort();
        collectRequestCountMetric(address);
    }

    private static void collectRequestCountMetric(String address) {
        Map<String, String> tagsMap = new HashMap<>();
        if (StringUtils.isEmpty(DubboCache.INSTANCE.getAppName())) {
            tagsMap.put(RouterConstant.CLIENT_SERVICE_NAME, AppCache.INSTANCE.getAppName());
        } else {
            tagsMap.put(RouterConstant.CLIENT_SERVICE_NAME, DubboCache.INSTANCE.getAppName());
        }
        tagsMap.put(RouterConstant.SERVER_ADDRESS, address);
        tagsMap.put(RouterConstant.PROTOCOL, RouterConstant.HTTP_PROTOCOL);
        addOrUpdateCounterMetricValue(RouterConstant.ROUTER_REQUEST_COUNT, tagsMap, 1);
    }

    /**
     * Get the key of the metric tag
     *
     * @param tagName tag name for routing rules or lane rules
     * @return the key of the metric tag
     */
    public static String getTagKeyByRouteTagOrLaneTag(String tagName) {
        return TAG_KEY_MAP.get(tagName);
    }

    /**
     * Get All the key of the metric tag
     *
     * @return All the key of the metric tag
     */
    public static Collection<String> getAllTagKey() {
        return TAG_KEY_MAP.values();
    }
}
