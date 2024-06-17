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
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.utils.StringUtils;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.mq.grayscale.config.Base;
import io.sermant.mq.grayscale.config.MessageFilter;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;
import io.sermant.mq.grayscale.strategy.TagKeyMatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * grayscale config util
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class MqGrayscaleConfigUtils {
    // environment message tag key
    public final static String MICRO_SERVICE_GRAY_TAG_KEY = "micro_service_gray_tag";

    // traffic message tag key
    public final static String MICRO_TRAFFIC_GRAY_TAG_KEY = "micro_traffic_gray_tag";

    // exclude tag change flag
    public static boolean MQ_EXCLUDE_TAG_CHANGE_FLAG = false;

    private final static Map<String, String> MICRO_SERVICE_PROPERTIES = new HashMap<>();

    private final static Map<String, MqGrayscaleConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    // config cache key
    public final static String CONFIG_CACHE_KEY = "mqGrayConfig";

    private final static String PERCENT_SIGN = "%";

    private final static long AUTO_CHECK_DELAY_TIME = 30L;

    static {
        ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        MICRO_SERVICE_PROPERTIES.put("version", serviceMeta.getVersion());
        if (serviceMeta.getParameters() != null) {
            MICRO_SERVICE_PROPERTIES.putAll(serviceMeta.getParameters());
        }
        CONFIG_CACHE.put(CONFIG_CACHE_KEY, new MqGrayscaleConfig());
    }

    private MqGrayscaleConfigUtils() {

    }

    /**
     * get gray environment message tag
     *
     * @return env tag
     */
    public static String getGrayEnvTag() {
        if (grayscaleDisabled()) {
            return "";
        }
        Map<String, List<String>> envMatch = CONFIG_CACHE.get(CONFIG_CACHE_KEY).getGrayscale().getEnvironmentMatch();
        if (envMatch == null) {
            return "";
        }
        String matchTag = TagKeyMatcher.getMatchTag(envMatch, MICRO_SERVICE_PROPERTIES);
        if (!StringUtils.isEmpty(matchTag)) {
            return standardFormatTag(matchTag);
        }
        return "";
    }

    /**
     * get gray traffic message tag
     *
     * @return traffic tag
     */
    public static String getTrafficGrayTag() {
        if (grayscaleDisabled()) {
            return "";
        }
        Map<String, List<String>> envMatch = CONFIG_CACHE.get(CONFIG_CACHE_KEY).getGrayscale().getTrafficMatch();
        if (envMatch == null) {
            return "";
        }
        String matchTag = TagKeyMatcher.getMatchTag(envMatch, buildTrafficTag());
        if (!StringUtils.isEmpty(matchTag)) {
            return standardFormatTag(matchTag);
        }
        return "";
    }

    /**
     * determine whether tag is include in excludeTags
     *
     * @param grayTag grayTag
     * @return is contains
     */
    public static boolean isExcludeTagsContainsTag(String grayTag) {
        if (StringUtils.isEmpty(grayTag)) {
            return false;
        }
        if (!grayBaseDisabled()) {
            Map<String, String> excludeTags
                    = CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter().getExcludeTags();
            if (!excludeTags.isEmpty()) {
                for (Map.Entry<String, String> entry: excludeTags.entrySet()) {
                    if (grayTag.equals(standardFormatTag(entry.getKey() + PERCENT_SIGN + entry.getValue()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * modify excludeTags
     *
     * @param excludeTags excludeTags
     */
    public static void modifyExcludeTags(Set<String> excludeTags) {
        if (excludeTags.isEmpty()) {
            return;
        }
        MQ_EXCLUDE_TAG_CHANGE_FLAG = true;
        if (CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase() == null) {
            CONFIG_CACHE.get(CONFIG_CACHE_KEY).setBase(new Base());
        }
        if (CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter() == null) {
            CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().setMessageFilter(new MessageFilter());
        }
        Map<String, String> configExcludeTags
                = CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter().getExcludeTags();
        for (String tag: excludeTags) {
            String tagName = tag.split(PERCENT_SIGN)[0];
            String tagValue = tag.split(PERCENT_SIGN)[1];
            configExcludeTags.put(tagName, tagValue);
        }
    }

    /**
     * get consume type
     *
     * @return consume type
     */
    public static String getConsumeType() {
        if (grayBaseDisabled()) {
            return "all";
        }
        return CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter().getConsumeType();
    }

    /**
     * get auto check delayTime
     *
     * @return delayTime
     */
    public static long getAutoCheckDelayTime() {
        if (grayBaseDisabled()) {
            return AUTO_CHECK_DELAY_TIME;
        }
        return CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter().getAutoCheckDelayTime();
    }

    /**
     * format tag
     *
     * @param tag tag
     * @return format tag
     */
    public static String standardFormatTag(String tag) {
        return tag.toLowerCase(Locale.ROOT).replaceAll("[^%|a-zA-Z0-9_-]", "-");
    }

    /**
     * build exclude tags for set
     *
     * @return tags for set
     */
    public static Set<String> getExcludeTagsForSet() {
        Set<String> excludeTags = new HashSet<>();
        if (grayBaseDisabled()) {
            return excludeTags;
        }
        Map<String, String> tags =
                CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter().getExcludeTags();
        for (Map.Entry<String, String> entry: tags.entrySet()) {
            excludeTags.add(standardFormatTag(entry.getKey() + PERCENT_SIGN + entry.getValue()));
        }
        return excludeTags;
    }

    private static Map<String, String> buildTrafficTag() {
        Map<String, String> map = new HashMap<>();
        if (TrafficUtils.getTrafficTag() != null && TrafficUtils.getTrafficTag().getTag() != null) {
            Map<String, List<String>> trafficTags = TrafficUtils.getTrafficTag().getTag();
            for (Map.Entry<String, List<String>> entry: trafficTags.entrySet()) {
                map.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return map;
    }

    /**
     * is mq using server gray message
     *
     * @return is enabled
     */
    public static boolean isMqServerGrayEnabled() {
        return CONFIG_CACHE.get(CONFIG_CACHE_KEY).isServerGrayEnabled();
    }

    private static boolean grayscaleDisabled() {
        return !CONFIG_CACHE.get(CONFIG_CACHE_KEY).isEnabled()
                || CONFIG_CACHE.get(CONFIG_CACHE_KEY).getGrayscale() == null;
    }

    private static boolean grayBaseDisabled() {
        return CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase() == null
                || CONFIG_CACHE.get(CONFIG_CACHE_KEY).getBase().getMessageFilter() == null;
    }

    /**
     * reset grayscale config
     */
    public static void resetGrayscaleConfig() {
        CONFIG_CACHE.put(CONFIG_CACHE_KEY, new MqGrayscaleConfig());
    }

    /**
     * set grayscale config
     *
     * @param config config
     */
    public static void setGrayscaleConfig(MqGrayscaleConfig config) {
        CONFIG_CACHE.put(CONFIG_CACHE_KEY, config);
    }

    /**
     * is plugin enabled
     *
     * @return enabled
     */
    public static boolean isPlugEnabled() {
        return CONFIG_CACHE.get(CONFIG_CACHE_KEY).isEnabled();
    }
}
