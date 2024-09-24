/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.rocketmq;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.config.BaseMessage;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqGrayConfigCache;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * build gray config test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class GrayConfigContextUtils {
    public static MqGrayscaleConfig buildGrayConfig(Map<String, Object> map) {
        MqGrayscaleConfig config = new MqGrayscaleConfig();
        config.setEnabled(true);
        BaseMessage base = new BaseMessage();
        base.setExcludeGroupTags(Collections.singletonList("gray"));
        base.setConsumeMode(map == null || map.get("consumeMode") == null ? ConsumeModeEnum.AUTO :
                (ConsumeModeEnum) map.get("consumeMode"));
        config.setBase(base);
        GrayTagItem item = new GrayTagItem();
        item.setConsumerGroupTag("gray");
        Map<String, String> serviceMeta = new HashMap<>();
        serviceMeta.put("x_lane_tag", map == null || map.get("x_lane_tag") == null ? "gray" : (String) map.get(
                "x_lane_tag"));
        item.setServiceMeta(serviceMeta);
        Map<String, String> trafficTag = new HashMap<>();
        trafficTag.put("x_lane_canary", map == null || map.get("x_lane_canary") == null ? "gray" : (String) map.get(
                "x_lane_canary"));
        item.setTrafficTag(trafficTag);
        List<GrayTagItem> grayscale = new ArrayList<>();
        grayscale.add(item);
        config.setGrayscale(grayscale);
        return config;
    }

    public static void createMqGrayConfig(Map<String, Object> map, DynamicConfigEventType eventType) {
        MqGrayConfigCache.setCacheConfig(buildGrayConfig(map), eventType);
    }
}
