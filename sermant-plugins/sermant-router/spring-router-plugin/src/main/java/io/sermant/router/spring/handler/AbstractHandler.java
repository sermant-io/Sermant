/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.handler;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.router.common.cache.AppCache;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.handler.Handler;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.spring.entity.Keys;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract Handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractHandler implements Handler {
    private final RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    /**
     * From the headers, obtain the request token that needs to be transparently transmitted
     *
     * @param headers HTTP request headers
     * @param keys The key of the tag to be obtained
     * @return Request tags
     */
    protected Map<String, List<String>> getRequestTag(Map<String, List<String>> headers, Set<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        for (String headerKey : keys) {
            if (headers.containsKey(headerKey)) {
                map.put(headerKey, headers.get(headerKey));
            }
        }
        return map;
    }

    /**
     * Obtain transparent tags
     *
     * @param path The path of the request
     * @param methodName http method
     * @param headers HTTP request headers
     * @param parameters URL parameter
     * @param keys The key of the tag to be obtained
     * @return Marks for transparent transmission
     */
    public abstract Map<String, List<String>> getRequestTag(String path, String methodName,
            Map<String, List<String>> headers, Map<String, List<String>> parameters, Keys keys);

    /**
     * Collect Lane Count Metric.
     *
     * @param laneTag lane tag
     */
    protected void collectLaneCountMetric(Map<String, List<String>> laneTag) {
        if (!routerConfig.isEnableMetric()) {
            return;
        }
        Set<String> tagValues = new HashSet<>();
        laneTag.forEach((key, values) -> {
            if (CollectionUtils.isEmpty(values)) {
                return;
            }
            values.forEach(tagValue -> {
                if (tagValues.contains(tagValue)) {
                    return;
                }
                tagValues.add(tagValue);
                Map<String, String> tagsMap = new HashMap<>();
                tagsMap.put(RouterConstant.LANE_TAG, key + ":" + tagValue);
                tagsMap.put(RouterConstant.CLIENT_SERVICE_NAME, AppCache.INSTANCE.getAppName());
                tagsMap.put(RouterConstant.PROTOCOL, RouterConstant.HTTP_PROTOCOL);
                MetricsManager.addOrUpdateCounterMetricValue(RouterConstant.LANE_TAG_COUNT, tagsMap, 1);
            });
        });
    }
}
