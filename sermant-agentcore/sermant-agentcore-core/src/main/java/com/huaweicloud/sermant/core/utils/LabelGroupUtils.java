/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * group generator
 *
 * @author zhouss
 * @since 2021-11-23
 */
public class LabelGroupUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String GROUP_SEPARATOR = "&";

    private static final String KV_SEPARATOR = "=";

    /**
     * The kv separator used in the query
     */
    private static final String LABEL_QUERY_SEPARATOR = ":";

    /**
     * Query label prefix
     */
    private static final String LABEL_PREFIX = "label=";

    /**
     * Key-value pair length
     */
    private static final int KV_LEN = 2;

    /**
     * Default group
     */
    private static final String DEFAULT_GROUP_KEY = "GROUP";

    private LabelGroupUtils() {
    }

    /**
     * Create Label Group
     *
     * @param labels labels
     * @return labelGroup such as: app=sc&service=helloService
     */
    public static String createLabelGroup(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return StringUtils.EMPTY;
        }
        final StringBuilder group = new StringBuilder();
        final List<String> keys = new ArrayList<>(labels.keySet());

        // Prevent the same map from having different labels in the end
        Collections.sort(keys);
        for (String key : keys) {
            String value = labels.get(key);
            if (key == null || value == null) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Invalid group label, key = %s, value = %s",
                        key, value));
                continue;
            }
            group.append(key).append(KV_SEPARATOR).append(value).append(GROUP_SEPARATOR);
        }
        if (group.length() == 0) {
            return StringUtils.EMPTY;
        }
        return group.deleteCharAt(group.length() - 1).toString();
    }

    /**
     * Reorganize groups to prevent groups from being different due to the order of multiple labels
     *
     * @param group group
     * @return group
     */
    public static String rebuildGroup(String group) {
        if (isLabelGroup(group)) {
            return createLabelGroup(resolveGroupLabels(group));
        }
        return LabelGroupUtils.createLabelGroup(Collections.singletonMap(DEFAULT_GROUP_KEY, group));
    }

    /**
     * Whether it is a label group key
     *
     * @param group group
     * @return result
     */
    public static boolean isLabelGroup(String group) {
        return group != null && group.contains(KV_SEPARATOR);
    }

    /**
     * Parse the label as map
     *
     * @param group group, such as: app=sc&service=helloService
     * @return Tag key-value pairs, and the return key values will be ordered
     */
    public static Map<String, String> resolveGroupLabels(String group) {
        final Map<String, String> result = new LinkedHashMap<>();
        if (group == null) {
            return result;
        }
        String curGroup = group;
        if (!isLabelGroup(curGroup)) {
            // If the label is not a group (applicable to the ZK configuration center scenario), create a label for
            // the group
            curGroup = LabelGroupUtils.createLabelGroup(Collections.singletonMap(DEFAULT_GROUP_KEY, curGroup));
        }
        try {
            final String decode = URLDecoder.decode(curGroup, "UTF-8");
            final String[] labels = decode.split("&");
            for (String label : labels) {
                final String[] labelKv = label.split("=");
                if (labelKv.length == KV_LEN) {
                    result.put(labelKv[0], labelKv[1]);
                } else if (labelKv.length == 1) {
                    // If only key is configured, use an empty string instead
                    result.put(labelKv[0], "");
                } else {
                    LOGGER.warning(String.format(Locale.ENGLISH, "Invalid label [%s]", label));
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "UnsupportedEncodingException, msg is {0}.", e.getMessage());
        }
        return result;
    }

    /**
     * get label condition
     *
     * @param group group, 'app=sc&service=helloService' is convert to 'label=app:sc&label=service:helloService'
     * @return label condition
     */
    public static String getLabelCondition(String group) {
        if (StringUtils.isEmpty(group)) {
            return group;
        }
        String curGroup = rebuildGroup(group);
        final Map<String, String> labels = resolveGroupLabels(curGroup);
        final StringBuilder finalGroup = new StringBuilder();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            finalGroup.append(LABEL_PREFIX)
                    .append(buildSingleLabel(entry.getKey(), entry.getValue()))
                    .append(GROUP_SEPARATOR);
        }
        return finalGroup.deleteCharAt(finalGroup.length() - 1).toString();
    }

    private static String buildSingleLabel(String key, String value) {
        try {
            return URLEncoder.encode(key + LABEL_QUERY_SEPARATOR + value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "UnsupportedEncodingException, msg is {0}.", e.getMessage());
            return StringUtils.EMPTY;
        }
    }
}
