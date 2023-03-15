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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.spring.service.LaneService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AbstractHandlerMapping处理器
 *
 * @author provenceee
 * @since 2023-02-21
 */
public class LaneMappingHandler extends AbstractMappingHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final LaneService laneService;

    /**
     * 构造方法
     */
    public LaneMappingHandler() {
        laneService = ServiceManager.getService(LaneService.class);
    }

    /**
     * 获取透传的标记
     *
     * @param path 请求路径
     * @param methodName http方法
     * @param headers http请求头
     * @param parameters url参数
     * @return 透传的标记
     */
    @Override
    public Map<String, List<String>> getRequestTag(String path, String methodName, Map<String, List<String>> headers,
        Map<String, List<String>> parameters) {
        Set<String> matchTags = configService.getMatchTags();
        if (CollectionUtils.isEmpty(matchTags)) {
            // 染色标记为空，代表没有染色规则，直接return
            LOGGER.fine("Lane tags are empty.");
            return Collections.emptyMap();
        }

        // 上游透传的标记
        Map<String, List<String>> tags = getRequestTag(headers, matchTags);

        // 本次染色标记
        Map<String, List<String>> laneTag = laneService.getLaneByParameterList(path, methodName, headers, parameters);
        if (CollectionUtils.isEmpty(laneTag)) {
            LOGGER.fine("Lane is empty.");
            return tags;
        }

        // 如果上游传来的标记中，存在与本次染色相同的标记，以上游传递的为准
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