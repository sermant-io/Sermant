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

package io.sermant.mq.grayscale.config.rocketmq;

import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rocketmq config utils
 *
 * @author chengyouling
 * @since 2024-09-13
 */
public class RocketMqConfigUtils {
    /**
     * base instance subscript gray tag change flags
     * key: namesrvAddr@topic@consumerGroup
     * value: change flag
     */
    private static final Map<String, Boolean> BASE_GROUP_TAG_CHANGE_MAP = new ConcurrentHashMap<>();

    /**
     * gray instance subscript gray tag change flags
     * key: namesrvAddr@topic@consumerGroup
     * value: change flag
     */
    private static final Map<String, Boolean> GRAY_GROUP_TAG_CHANGE_MAP = new ConcurrentHashMap<>();

    /**
     * all traffic tags that's been set, using for sql92 expression reset
     */
    private static final Set<String> GRAY_TAGS_SET = new HashSet<>();

    private RocketMqConfigUtils() {
    }

    /**
     * set base consumer address@topic@group correspondents change flag
     *
     * @param subscribeScope subscribeScope
     * @param flag flag
     */
    public static void setBaseGroupTagChangeMap(String subscribeScope, boolean flag) {
        BASE_GROUP_TAG_CHANGE_MAP.put(subscribeScope, flag);
    }

    /**
     * set gray consumer address@topic@group correspondents change flag
     *
     * @param subscribeScope subscribeScope
     * @param flag flag
     */
    public static void setGrayGroupTagChangeMap(String subscribeScope, boolean flag) {
        GRAY_GROUP_TAG_CHANGE_MAP.put(subscribeScope, flag);
    }

    /**
     * get base consumer address@topic@group correspondents change flag
     *
     * @param subscribeScope subscribeScope
     * @return changeFlag
     */
    public static boolean getBaseGroupTagChangeMap(String subscribeScope) {
        return BASE_GROUP_TAG_CHANGE_MAP.get(subscribeScope) != null && BASE_GROUP_TAG_CHANGE_MAP.get(subscribeScope);
    }

    /**
     * get gray consumer address@topic@group correspondents change flag
     *
     * @param subscribeScope subscribeScope
     * @return changeFlag
     */
    public static boolean getGrayGroupTagChangeMap(String subscribeScope) {
        return GRAY_GROUP_TAG_CHANGE_MAP.get(subscribeScope) != null && GRAY_GROUP_TAG_CHANGE_MAP.get(subscribeScope);
    }

    /**
     * update all consumer gray tag change flag
     */
    public static void updateChangeFlag() {
        BASE_GROUP_TAG_CHANGE_MAP.replaceAll((k, v) -> true);
        GRAY_GROUP_TAG_CHANGE_MAP.replaceAll((k, v) -> true);
    }

    /**
     * records traffic labels for historical and current configuration settings
     *
     * @param config config
     */
    public static void recordTrafficTagsSet(MqGrayscaleConfig config) {
        for (GrayTagItem item : config.getGrayscale()) {
            if (!item.getTrafficTag().isEmpty()) {
                GRAY_TAGS_SET.addAll(item.getTrafficTag().keySet());
            }
        }
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
