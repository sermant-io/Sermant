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

package io.sermant.mq.grayscale.utils;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;

import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * grayscale config util
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class MqGrayscaleConfigUtils {
    /**
     * serviceMeta info
     */
    public static final Map<String, String> MICRO_SERVICE_PROPERTIES = new HashMap<>();

    private static MqGrayscaleConfig cacheConfig = PluginConfigManager.getPluginConfig(MqGrayscaleConfig.class);

    /**
     * all traffic tags that's been set, using for sql92 expression reset
     */
    private static final Set<String> GRAY_TAGS_SET = new HashSet<>();

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

    private MqGrayscaleConfigUtils() {
    }

    /**
     * compare mqGrayscaleConfig with serviceMeta, return match grayGroupTag
     *
     * @return grayGroupTag
     */
    public static String getGrayGroupTag() {
        if (!cacheConfig.isEnabled()) {
            return "";
        }
        Optional<GrayTagItem> itemOptional = cacheConfig.getMatchedGrayTagByServiceMeta(MICRO_SERVICE_PROPERTIES);
        return itemOptional.map(grayTagItem -> standardFormatGroupTag(grayTagItem.getConsumerGroupTag())).orElse("");
    }

    /**
     * get current consumerType
     *
     * @return consumeType
     */
    public static ConsumeModeEnum getConsumeType() {
        return cacheConfig.getBase().getConsumeMode();
    }

    /**
     * get interval for scheduler find gray consumer
     *
     * @return delayTime
     */
    public static long getAutoCheckDelayTime() {
        return cacheConfig.getBase().getAutoCheckDelayTime();
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
     * reset cache mqGrayscaleConfig
     */
    public static void resetGrayscaleConfig() {
        cacheConfig = new MqGrayscaleConfig();
    }

    /**
     * set/update cache mqGrayscaleConfig
     *
     * @param config config
     * @param eventType eventType
     */
    public static void setGrayscaleConfig(MqGrayscaleConfig config, DynamicConfigEventType eventType) {
        buildGrayTagsSet(config);
        if (eventType == DynamicConfigEventType.CREATE) {
            cacheConfig = config;
            SubscriptionDataUtils.updateChangeFlag();
            return;
        }
        boolean isAllowRefresh = isAllowRefreshChangeFlag(cacheConfig, config);
        if (isAllowRefresh) {
            cacheConfig.updateGrayscaleConfig(config);
            SubscriptionDataUtils.updateChangeFlag();
        }
    }

    private static void buildGrayTagsSet(MqGrayscaleConfig config) {
        for (GrayTagItem item : config.getGrayscale()) {
            if (!item.getTrafficTag().isEmpty()) {
                GRAY_TAGS_SET.addAll(item.getTrafficTag().keySet());
            }
        }
    }

    /**
     * only traffic label changes allow refresh tag change map to rebuild SQL92 query statement,
     * because if the serviceMeta changed, the gray consumer cannot be matched and becomes a base consumer
     * so, if you need to change the env tag, restart all services.
     *
     * @param resource cache config
     * @param target cache config
     * @return boolean
     */
    private static boolean isAllowRefreshChangeFlag(MqGrayscaleConfig resource, MqGrayscaleConfig target) {
        if (resource.isEnabled() != target.isEnabled()) {
            return true;
        }
        if (resource.isBaseExcludeGroupTagsChanged(target)) {
            return true;
        }
        if (resource.isConsumerModeChanged(target)) {
            return true;
        }
        return !resource.buildAllTrafficTagInfoToStr().equals(target.buildAllTrafficTagInfoToStr());
    }

    /**
     * get plugin enabled
     *
     * @return plugin enabled
     */
    public static boolean isPluginEnabled() {
        return cacheConfig.isEnabled();
    }

    /**
     * compare serviceMeta with mqGrayscaleConfig, set message property
     *
     * @param message message
     */
    public static void setUserPropertyByServiceMeta(Message message) {
        if (!cacheConfig.isEnabled()) {
            return;
        }
        Map<String, String> grayTags = cacheConfig.getGrayTagsByServiceMeta(MICRO_SERVICE_PROPERTIES);
        if (grayTags.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : grayTags.entrySet()) {
            message.putUserProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * get cache mqGrayscaleConfig
     *
     * @return MqGrayscaleConfig
     */
    public static MqGrayscaleConfig getGrayscaleConfigs() {
        return cacheConfig;
    }

    /**
     * get GrayTagItems by set excludeGroupTags
     *
     * @return GrayTagItems
     */
    public static List<GrayTagItem> getGrayTagItemByExcludeGroupTags() {
        MqGrayscaleConfig mqGrayscaleConfig = getGrayscaleConfigs();
        if (mqGrayscaleConfig.getBase() == null || mqGrayscaleConfig.getBase().getExcludeGroupTags().isEmpty()) {
            return Collections.emptyList();
        }
        List<GrayTagItem> result = new ArrayList<>();
        for (String excludeGroupTag : mqGrayscaleConfig.getBase().getExcludeGroupTags()) {
            if (mqGrayscaleConfig.getGrayTagByGroupTag(excludeGroupTag).isPresent()) {
                result.add(mqGrayscaleConfig.getGrayTagByGroupTag(excludeGroupTag).get());
            }
        }
        return result;
    }

    /**
     * get all config set gray tags
     *
     * @return all config set gray tags
     */
    public static Set<String> getGrayTagsSet() {
        return GRAY_TAGS_SET;
    }
}
