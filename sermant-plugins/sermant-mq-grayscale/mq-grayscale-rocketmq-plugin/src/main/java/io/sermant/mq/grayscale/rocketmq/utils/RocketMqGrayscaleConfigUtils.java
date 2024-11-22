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

package io.sermant.mq.grayscale.rocketmq.utils;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqGrayConfigCache;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;

import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * grayscale config util
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class RocketMqGrayscaleConfigUtils {
    /**
     * serviceMeta info
     */
    private static final Map<String, String> MICRO_SERVICE_PROPERTIES = new HashMap<>();

    /**
     * consumerGroup name rule: ^[%|a-zA-Z0-9_-]+$
     */
    private static final Pattern PATTERN = Pattern.compile("[^%|a-zA-Z0-9_-]");

    static {
        ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        MICRO_SERVICE_PROPERTIES.put("version", serviceMeta.getVersion());
        if (serviceMeta.getParameters() != null) {
            MICRO_SERVICE_PROPERTIES.putAll(serviceMeta.getParameters());
        }
    }

    private RocketMqGrayscaleConfigUtils() {
    }

    /**
     * compare mqGrayscaleConfig with serviceMeta, return match grayGroupTag
     *
     * @return grayGroupTag
     */
    public static String getGrayGroupTag() {
        if (!MqGrayConfigCache.getCacheConfig().isEnabled()) {
            return "";
        }
        Optional<GrayTagItem> itemOptional
                = MqGrayConfigCache.getCacheConfig().getMatchedGrayTagByServiceMeta(MICRO_SERVICE_PROPERTIES);
        return itemOptional.map(grayTagItem -> standardFormatGroupTag(grayTagItem.getConsumerGroupTag())).orElse("");
    }

    /**
     * get current consumerType
     *
     * @return consumeType
     */
    public static ConsumeModeEnum getConsumeType() {
        return MqGrayConfigCache.getCacheConfig().getBase().getConsumeMode();
    }

    /**
     * get interval for scheduler find gray consumer
     *
     * @return delayTime
     */
    public static long getAutoCheckDelayTime() {
        return MqGrayConfigCache.getCacheConfig().getBase().getAutoCheckDelayTime();
    }

    /**
     * format grayGroupTag
     *
     * @param grayGroupTag grayGroupTag
     * @return standard grayGroupTag
     */
    public static String standardFormatGroupTag(String grayGroupTag) {
        return PATTERN.matcher(grayGroupTag.toLowerCase(Locale.ROOT)).replaceAll("-");
    }

    /**
     * compare serviceMeta with mqGrayscaleConfig, set message property
     *
     * @param message message
     */
    public static void injectTrafficTagByServiceMeta(Message message) {
        if (!MqGrayConfigCache.getCacheConfig().isEnabled()) {
            return;
        }
        Map<String, String> grayTags
                = MqGrayConfigCache.getCacheConfig().getGrayTagsByServiceMeta(MICRO_SERVICE_PROPERTIES);
        if (grayTags.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : grayTags.entrySet()) {
            if (message.getProperties() == null || !message.getProperties().containsKey(entry.getKey())) {
                message.putUserProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * get GrayTagItems by set excludeGroupTags
     *
     * @return GrayTagItems
     */
    public static List<GrayTagItem> getGrayTagItemByExcludeGroupTags() {
        MqGrayscaleConfig mqGrayscaleConfig = MqGrayConfigCache.getCacheConfig();
        List<GrayTagItem> result = new ArrayList<>();
        if (mqGrayscaleConfig.getBase() == null || mqGrayscaleConfig.getBase().getExcludeGroupTags().isEmpty()) {
            return result;
        }
        for (String excludeGroupTag : mqGrayscaleConfig.getBase().getExcludeGroupTags()) {
            if (mqGrayscaleConfig.getGrayTagByGroupTag(excludeGroupTag).isPresent()) {
                result.add(mqGrayscaleConfig.getGrayTagByGroupTag(excludeGroupTag).get());
            }
        }
        return result;
    }
}
