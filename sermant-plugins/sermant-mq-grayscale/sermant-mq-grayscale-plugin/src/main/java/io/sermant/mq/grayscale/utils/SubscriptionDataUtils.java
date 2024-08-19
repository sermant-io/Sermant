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

import io.sermant.core.common.LoggerFactory;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqConsumerClientConfig;
import io.sermant.mq.grayscale.config.MqGrayscaleConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * TAG/SQL92 query message statement builder util
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class SubscriptionDataUtils {
    /**
     * tag consume message type
     */
    public static final String EXPRESSION_TYPE_TAG = "TAG";

    /**
     * sql92 consume message type
     */
    public static final String EXPRESSION_TYPE_SQL92 = "SQL92";

    /**
     * afa symbol
     */
    public static final String AFA_SYMBOL = "@";

    /**
     * select all message sql92 expression
     */
    public static final String SELECT_ALL_MESSAGE_SQL = "(_message_tag_ is null) or (_message_tag_ is not null)";

    private static final Pattern PATTERN = Pattern.compile("and|or", Pattern.CASE_INSENSITIVE);

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * auto find gray consumer group correspondents grayscale config at auto model
     * key: namesrvAddr@topic@consumerGroup
     * value: grayTagItems
     */
    private static final Map<String, List<GrayTagItem>> AUTO_CHECK_GRAY_TAGS = new ConcurrentHashMap<>();

    /**
     * base instance subscript gray tag change flags
     * key: namesrvAddr@topic@consumerGroup
     * value: change flag
     */
    private static final Map<String, Boolean> BASE_SUBSCRIPT_GRAY_TAG_CHANGE_MAP = new ConcurrentHashMap<>();

    /**
     * gray instance subscript gray tag change flags
     * key: namesrvAddr@topic@consumerGroup
     * value: change flag
     */
    private static final Map<String, Boolean> GRAY_GROUP_TAG_CHANGE_MAP = new ConcurrentHashMap<>();

    private static final String RETYPE = "%RETRY%";

    private static final String RIGHT_BRACKET = ")";

    private static final String LEFT_BRACKET = "(";

    private static final String AND_SPLICE_STR = " and ";

    private SubscriptionDataUtils() {
    }

    /**
     * build sql expression by tag expression tags
     *
     * @param tagsSet tagsSet
     * @return sql92 statement
     */
    public static String buildSql92ExpressionByTags(Set<String> tagsSet) {
        return tagsSet != null && !tagsSet.isEmpty() ? buildTagsExpression(tagsSet) : "";
    }

    private static String buildTagsExpression(Set<String> tagsSet) {
        return "(TAGS is not null and TAGS in " + getStrForSets(tagsSet) + RIGHT_BRACKET;
    }

    private static String getStrForSets(Set<String> tags) {
        StringBuilder builder = new StringBuilder(LEFT_BRACKET);
        for (String tag : tags) {
            builder.append("'").append(tag).append("'");
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(RIGHT_BRACKET);
        return builder.toString();
    }

    /**
     * add current gray tags to sql92 expression
     *
     * @param originSubData originSubData
     * @param addrTopicGroupKey addrTopicGroupKey
     * @return sql expression
     */
    public static String addGrayTagsToSql92Expression(String originSubData, String addrTopicGroupKey) {
        String originSubDataBak = originSubData;
        if (!StringUtils.isBlank(originSubDataBak)) {
            originSubDataBak = rebuildWithoutGrayTagSubData(originSubDataBak);
        }
        String sql92Expression = buildSql92Expression(addrTopicGroupKey);
        if (StringUtils.isBlank(sql92Expression)) {
            return originSubDataBak;
        }
        return StringUtils.isBlank(originSubDataBak)
                ? sql92Expression : originSubDataBak + AND_SPLICE_STR + sql92Expression;
    }

    private static String buildSql92Expression(String addrTopicGroupKey) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(MqGrayscaleConfigUtils.getGrayGroupTag())) {
            // base model return without exclude group message
            if (MqGrayscaleConfigUtils.getConsumeType() == ConsumeModeEnum.BASE) {
                List<GrayTagItem> items = MqGrayscaleConfigUtils.getGrayTagItemByExcludeGroupTags();
                if (!items.isEmpty()) {
                    sb.append(buildBaseConsumerSql92Expression(items));
                }
                return sb.toString();
            }

            // auto model return without exclude group and current consume message gray group message
            sb.append(buildBaseConsumerSql92Expression(getAutoTypeGrayTagItems(addrTopicGroupKey)));
        } else {
            MqGrayscaleConfig mqGrayscaleConfig = MqGrayscaleConfigUtils.getGrayscaleConfigs();
            Optional<GrayTagItem> grayTagItem
                    = mqGrayscaleConfig.getGrayTagByGroupTag(MqGrayscaleConfigUtils.getGrayGroupTag());
            if (grayTagItem.isPresent()) {
                sb.append(buildGrayConsumerSql92Expression(grayTagItem.get()));
            } else {
                LOGGER.warning(String.format(Locale.ENGLISH, "current gray group [%s] had not set grayscale, set it "
                        + "and restart service to valid.", MqGrayscaleConfigUtils.getGrayGroupTag()));
            }
        }
        return sb.toString();
    }

    private static List<GrayTagItem> getAutoTypeGrayTagItems(String addrTopicGroupKey) {
        List<GrayTagItem> excludeItems = MqGrayscaleConfigUtils.getGrayTagItemByExcludeGroupTags();
        List<GrayTagItem> autoDiscoveryGrayTags = AUTO_CHECK_GRAY_TAGS.get(addrTopicGroupKey);
        if (autoDiscoveryGrayTags != null && !autoDiscoveryGrayTags.isEmpty()) {
            excludeItems.addAll(autoDiscoveryGrayTags);
        }
        return excludeItems;
    }

    private static String buildGrayConsumerSql92Expression(GrayTagItem item) {
        Map<String, List<String>> trafficTagMap = new HashMap<>();
        for (Map.Entry<String, String> entry : item.getTrafficTag().entrySet()) {
            buildTrafficTagMap(trafficTagMap, entry);
        }
        StringBuilder builder = new StringBuilder();
        if (trafficTagMap.size() > 1) {
            builder.append(LEFT_BRACKET);
        }
        for (Map.Entry<String, List<String>> envEntry : trafficTagMap.entrySet()) {
            if (builder.length() > 1) {
                builder.append(" or ");
            }
            builder.append(LEFT_BRACKET)
                    .append(envEntry.getKey())
                    .append(" in ")
                    .append(getStrForSets(new HashSet<>(envEntry.getValue())))
                    .append(RIGHT_BRACKET);
        }
        if (trafficTagMap.size() > 1) {
            builder.append(RIGHT_BRACKET);
        }
        return builder.toString();
    }

    private static String buildBaseConsumerSql92Expression(List<GrayTagItem> items) {
        Map<String, List<String>> trafficTagMap = new HashMap<>();
        for (GrayTagItem item : items) {
            for (Map.Entry<String, String> entry : item.getTrafficTag().entrySet()) {
                buildTrafficTagMap(trafficTagMap, entry);
            }
        }
        StringBuilder builder = new StringBuilder();
        if (trafficTagMap.size() > 1) {
            builder.append(LEFT_BRACKET);
        }
        for (Map.Entry<String, List<String>> envEntry : trafficTagMap.entrySet()) {
            if (builder.length() > 1) {
                builder.append(AND_SPLICE_STR);
            }
            if (trafficTagMap.size() > 1) {
                builder.append(LEFT_BRACKET);
            }
            builder.append(LEFT_BRACKET)
                    .append(envEntry.getKey())
                    .append(" not in ")
                    .append(getStrForSets(new HashSet<>(envEntry.getValue())))
                    .append(RIGHT_BRACKET)
                    .append(" or ")
                    .append(LEFT_BRACKET)
                    .append(envEntry.getKey())
                    .append(" is null")
                    .append(RIGHT_BRACKET);
            if (trafficTagMap.size() > 1) {
                builder.append(RIGHT_BRACKET);
            }
        }
        if (trafficTagMap.size() > 1) {
            builder.append(RIGHT_BRACKET);
        }
        return builder.toString();
    }

    private static void buildTrafficTagMap(Map<String, List<String>> trafficTagMap, Map.Entry<String, String> entry) {
        trafficTagMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
    }

    private static String rebuildWithoutGrayTagSubData(String originSubData) {
        if (StringUtils.isBlank(originSubData)) {
            return originSubData;
        }
        String[] originConditions = PATTERN.split(originSubData);
        List<String> refactorConditions = new ArrayList<>();
        for (String condition: originConditions) {
            if (!containsGrayTags(condition) && !condition.contains("_message_tag_")) {
                refactorConditions.add(condition);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < refactorConditions.size(); i++) {
            sb.append(refactorConditions.get(i));
            if (i != refactorConditions.size() - 1) {
                sb.append(AND_SPLICE_STR);
            }
        }
        return sb.toString();
    }

    private static boolean containsGrayTags(String condition) {
        for (String key : MqGrayscaleConfigUtils.getGrayTagsSet()) {
            if (condition.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * reset subString with gray tags
     *
     * @param subscriptionData subscriptionData
     * @param addrTopicGroupKey addrTopicGroupKey
     */
    public static void resetsSql92SubscriptionData(SubscriptionData subscriptionData, String addrTopicGroupKey) {
        String originSubData;
        if (EXPRESSION_TYPE_TAG.equals(subscriptionData.getExpressionType())) {
            originSubData = buildSql92ExpressionByTags(subscriptionData.getTagsSet());
        } else {
            originSubData = subscriptionData.getSubString();
        }
        String newSubStr = addGrayTagsToSql92Expression(originSubData, addrTopicGroupKey);
        if (StringUtils.isEmpty(newSubStr)) {
            newSubStr = SELECT_ALL_MESSAGE_SQL;
        }
        if (EXPRESSION_TYPE_TAG.equals(subscriptionData.getExpressionType())) {
            subscriptionData.setExpressionType("SQL92");
            subscriptionData.getTagsSet().clear();
            subscriptionData.getCodeSet().clear();
        }
        subscriptionData.setSubString(newSubStr);
        subscriptionData.setSubVersion(System.currentTimeMillis());
        LOGGER.warning(String.format(Locale.ENGLISH, "update [key: %s] SQL92 subscriptionData, originSubStr: "
                + "[%s], newSubStr: [%s]", addrTopicGroupKey, originSubData, newSubStr));
    }

    /**
     * reset grayTagItems for address@topic@group
     *
     * @param grayTagItems grayTagItems
     * @param clientConfig clientConfig
     */
    public static void resetAutoCheckGrayTagItems(List<GrayTagItem> grayTagItems, MqConsumerClientConfig clientConfig) {
        String addrTopicGroupKey = buildAddrTopicGroupKey(clientConfig.getTopic(), clientConfig.getConsumerGroup(),
                clientConfig.getAddress());
        AUTO_CHECK_GRAY_TAGS.remove(addrTopicGroupKey);
        setAutoCheckTagChangeMap(clientConfig.getAddress(), clientConfig.getTopic(), clientConfig.getConsumerGroup(),
                true);
        if (!grayTagItems.isEmpty()) {
            AUTO_CHECK_GRAY_TAGS.put(addrTopicGroupKey, grayTagItems);
        }
    }

    /**
     * set base consumer address@topic@group correspondents change flag
     *
     * @param namesrvAddr namesrvAddr
     * @param topic topic
     * @param group group
     * @param flag flag
     */
    public static void setAutoCheckTagChangeMap(String namesrvAddr, String topic, String group, boolean flag) {
        String addrTopicGroupKey = buildAddrTopicGroupKey(topic, group, namesrvAddr);
        BASE_SUBSCRIPT_GRAY_TAG_CHANGE_MAP.put(addrTopicGroupKey, flag);
    }

    /**
     * set gray consumer address@topic@group correspondents change flag
     *
     * @param namesrvAddr namesrvAddr
     * @param topic topic
     * @param group group
     * @param flag flag
     */
    public static void setGrayGroupTagChangeMap(String namesrvAddr, String topic, String group, boolean flag) {
        String addrTopicGroupKey = buildAddrTopicGroupKey(topic, group, namesrvAddr);
        GRAY_GROUP_TAG_CHANGE_MAP.put(addrTopicGroupKey, flag);
    }

    /**
     * using namesrvAddr/topic/consumerGroup build key
     *
     * @param topic topic
     * @param consumerGroup consumerGroup
     * @param namesrvAddr namesrvAddr
     * @return namesrvAddr@topic@consumerGroup
     */
    public static String buildAddrTopicGroupKey(String topic, String consumerGroup, String namesrvAddr) {
        String topicTemp = topic.contains(RETYPE) ? StringUtils.substringAfterLast(topic, RETYPE) : topic;
        String consumerGroupTemp = consumerGroup.contains(RETYPE)
                ? StringUtils.substringAfterLast(consumerGroup, RETYPE) : consumerGroup;
        return namesrvAddr + AFA_SYMBOL + topicTemp + AFA_SYMBOL + consumerGroupTemp;
    }

    /**
     * get gray tag change flag
     *
     * @param topic topic
     * @param rebalance rebalance
     * @return changeFlag
     */
    public static boolean getGrayTagChangeFlag(String topic, RebalanceImpl rebalance) {
        String addrTopicGroupKey = buildAddrTopicGroupKey(topic, rebalance.getConsumerGroup(),
                rebalance.getmQClientFactory().getClientConfig().getNamesrvAddr());
        if (StringUtils.isEmpty(MqGrayscaleConfigUtils.getGrayGroupTag())) {
            return BASE_SUBSCRIPT_GRAY_TAG_CHANGE_MAP.get(addrTopicGroupKey) != null
                    && BASE_SUBSCRIPT_GRAY_TAG_CHANGE_MAP.get(addrTopicGroupKey);
        }
        return GRAY_GROUP_TAG_CHANGE_MAP.get(addrTopicGroupKey) != null
                && GRAY_GROUP_TAG_CHANGE_MAP.get(addrTopicGroupKey);
    }

    /**
     * update all consumer gray tag change flag
     */
    public static void updateChangeFlag() {
        BASE_SUBSCRIPT_GRAY_TAG_CHANGE_MAP.replaceAll((k, v) -> true);
        GRAY_GROUP_TAG_CHANGE_MAP.replaceAll((k, v) -> true);
    }

    /**
     * reset gray tag change flag
     *
     * @param namesrvAddr namesrvAddr
     * @param topic topic
     * @param consumerGroup consumerGroup
     * @param flag flag
     */
    public static void resetTagChangeMap(String namesrvAddr, String topic, String consumerGroup, boolean flag) {
        if (StringUtils.isEmpty(MqGrayscaleConfigUtils.getGrayGroupTag())) {
            setAutoCheckTagChangeMap(namesrvAddr, topic, consumerGroup, flag);
        } else {
            setGrayGroupTagChangeMap(namesrvAddr, topic, consumerGroup, flag);
        }
    }
}
