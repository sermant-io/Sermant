/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.spring.service.LaneService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stain web blocker handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public class LaneRequestTagHandler extends AbstractRequestTagHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final LaneService laneService;

    /**
     * Constructor
     */
    public LaneRequestTagHandler() {
        laneService = PluginServiceManager.getPluginService(LaneService.class);
    }

    /**
     * Obtain transparent tags
     *
     * @param path The path of the request
     * @param methodName The name of the method
     * @param headers HTTP request headers
     * @return Marks for transparent transmission
     */
    @Override
    public Map<String, List<String>> getRequestTag(String path, String methodName, Map<String, List<String>> headers,
            Map<String, String[]> parameters, Keys keys) {
        Set<String> injectTags = keys.getInjectTags();
        if (CollectionUtils.isEmpty(injectTags)) {
            // The staining mark is empty, which means that there are no staining rules, and it is returned directly
            LOGGER.fine("Lane tags are empty.");
            return Collections.emptyMap();
        }

        // Markers for upstream transparent transmissions
        Map<String, List<String>> tags = getRequestTag(headers, injectTags);

        // This staining marker
        Map<String, List<String>> laneTag = laneService.getLaneByParameterArray(path, methodName, headers, parameters);
        if (CollectionUtils.isEmpty(laneTag)) {
            LOGGER.fine("Lane is empty.");
            return tags;
        }

        // If there is a marker in the upstream transmission that is the same as the one in this staining,
        // the upstream transmission shall prevail
        laneTag.forEach(tags::putIfAbsent);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Lane is " + tags);
        }
        return tags;
    }

    @Override
    public int getOrder() {
        return RouterConstant.LANE_HANDLER_ORDER;
    }
}