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
import io.sermant.core.utils.StringUtils;

import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * TAG/SQL92 query message statement builder util
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class SubscriptionDataUtils {
    // TAG expression type
    public static final String EXPRESSION_TYPE_TAG = "TAG";

    // SQL92 expression type
    public static final String EXPRESSION_TYPE_SQL92 = "SQL92";

    private static final Pattern pattern = Pattern.compile("and|or", Pattern.CASE_INSENSITIVE);

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String CONSUME_TYPE_ALL = "all";

    private static final String CONSUME_TYPE_BASE = "base";

    private static final String PARENTHESES_NO_SPACE_RIGHT = " )";

    private static final String PARENTHESES_WITH_SPACE = " ) ";

    private static final String DOUBLE_PARENTHESES_WITH_SPACE = " ( ( ";

    private SubscriptionDataUtils() {
    }

    /**
     * build SQL92 query statement for tags set
     *
     * @param tagsSet tags
     * @return sql
     */
    public static String buildSql92ExpressionByTags(Set<String> tagsSet) {
        return tagsSet != null && !tagsSet.isEmpty() ? buildTagsExpression(tagsSet) : " ";
    }

    private static String buildTagsExpression(Set<String> tagsSet) {
        return  " ( TAGS is not null and TAGS in " + getStrForSets(tagsSet) + PARENTHESES_WITH_SPACE;
    }

    private static String getStrForSets(Set<String> tags) {
        StringBuilder builder = new StringBuilder("(");
        for (String tag : tags) {
            builder.append("'").append(tag).append("'");
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(") ");
        return builder.toString();
    }

    /**
     * SQL92 query statement add gray tags
     *
     * @param originSubData origin data
     * @return sql
     */
    public static String addMseGrayTagsToSql92Expression(String originSubData) {
        if (!StringUtils.isBlank(originSubData)) {
            originSubData = removeMseGrayTagFromOriginSubData(originSubData);
        }
        String sql92Expression = buildSql92Expression();
        if (StringUtils.isBlank(sql92Expression)) {
            return originSubData;
        } else {
            return StringUtils.isBlank(originSubData) ? sql92Expression : originSubData + " and " + sql92Expression;
        }
    }

    private static String buildSql92Expression() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(MqGrayscaleConfigUtils.getGrayEnvTag())) {

            // "all" consume type return ""
            if (CONSUME_TYPE_ALL.equals(MqGrayscaleConfigUtils.getConsumeType())) {
                return "";
            }

            // "base" consume type only return without tag message
            if (CONSUME_TYPE_BASE.equals(MqGrayscaleConfigUtils.getConsumeType())) {
                sb.append(DOUBLE_PARENTHESES_WITH_SPACE)
                        .append(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY)
                        .append(" is null ")
                        .append(PARENTHESES_NO_SPACE_RIGHT)
                        .append(" and ( ")
                        .append(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY)
                        .append(" is null ) ")
                        .append(PARENTHESES_WITH_SPACE);
                return sb.toString();
            }

            // if exists traffic tag at "auto" consume type, only return traffic tag message
            if (!StringUtils.isEmpty(MqGrayscaleConfigUtils.getTrafficGrayTag())) {
                Set<String> trafficSet = new HashSet<>();
                trafficSet.add(MqGrayscaleConfigUtils.getTrafficGrayTag());
                String trafficGrayTag = getStrForSets(trafficSet);
                sb.append(" ( ")
                        .append(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY)
                        .append(" in ")
                        .append(trafficGrayTag)
                        .append(PARENTHESES_NO_SPACE_RIGHT);
                return sb.toString();
            }
            sb.append(DOUBLE_PARENTHESES_WITH_SPACE)
                    .append(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY)
                    .append(" not in ")
                    .append(getStrForSets(MqGrayscaleConfigUtils.getExcludeTagsForSet()))
                    .append(PARENTHESES_NO_SPACE_RIGHT)
                    .append(" or ( ")
                    .append(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY)
                    .append(" is null ) ")
                    .append(PARENTHESES_WITH_SPACE);
        } else {
            buildGrayEnvTagsSql(sb);
        }
        return sb.toString();
    }

    private static void buildGrayEnvTagsSql(StringBuilder sb) {
        Set<String> set = new HashSet<>();
        set.add(MqGrayscaleConfigUtils.getGrayEnvTag());
        String envGrayTag = getStrForSets(set);
        sb.append(DOUBLE_PARENTHESES_WITH_SPACE)
                .append(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY)
                .append(" in ")
                .append(envGrayTag)
                .append(")")
                .append(" or ( ")
                .append(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY)
                .append(" is not null ) ")
                .append(PARENTHESES_WITH_SPACE);
    }

    private static String removeMseGrayTagFromOriginSubData(String originSubData) {
        if (StringUtils.isBlank(originSubData)) {
            return originSubData;
        }
        String[] originConditions = pattern.split(originSubData);
        List<String> refactorConditions = new ArrayList<>();
        for (String condition: originConditions) {
            if (!condition.contains(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY)
                    && !condition.contains(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY)) {
                refactorConditions.add(condition);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < refactorConditions.size(); i++) {
            sb.append(refactorConditions.get(i));
            if (i != refactorConditions.size() - 1) {
                sb.append(" AND ");
            }
        }
        return sb.toString();
    }

    /**
     * resets SQL92 subscription data
     *
     * @param subscriptionData subscription data
     */
    public static void resetsSql92SubscriptionData(SubscriptionData subscriptionData) {
        String originSubData = buildSql92ExpressionByTags(subscriptionData.getTagsSet());
        String subStr = addMseGrayTagsToSql92Expression(originSubData);
        if (StringUtils.isEmpty(subStr)) {
            subStr = "( " + MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY + "  is null ) or ( "
                    + MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY + "  is not null )";
        }
        subscriptionData.setExpressionType(EXPRESSION_TYPE_SQL92);
        subscriptionData.getTagsSet().clear();
        subscriptionData.getCodeSet().clear();
        subscriptionData.setSubString(subStr);
        subscriptionData.setSubVersion(System.currentTimeMillis());
        LOGGER.warning(String.format(Locale.ENGLISH, "update TAG to SQL92 subscriptionData, originSubStr: "
                + "%s, newSubStr: %s", originSubData, subStr));
    }
}
