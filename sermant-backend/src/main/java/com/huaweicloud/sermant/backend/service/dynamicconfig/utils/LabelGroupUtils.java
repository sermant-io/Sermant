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

package com.huaweicloud.sermant.backend.service.dynamicconfig.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * group生成工具
 *
 * @author zhouss
 * @since 2021-11-23
 */
public class LabelGroupUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelGroupUtils.class);

    private static final String GROUP_SEPARATOR = "&";

    private static final String KV_SEPARATOR = "=";

    /**
     * 查询时使用的kv分隔符
     */
    private static final String LABEL_QUERY_SEPARATOR = ":";

    /**
     * 查询标签前缀
     */
    private static final String LABEL_PREFIX = "label=";

    /**
     * 键值对长度
     */
    private static final int KV_LEN = 2;

    private LabelGroupUtils() {
    }

    /**
     * 创建标签组
     *
     * @param labels 标签组
     * @return labelGroup 例如: app=sc&service=helloService
     */
    public static String createLabelGroup(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return StringUtils.EMPTY;
        }
        final StringBuilder group = new StringBuilder();
        final List<String> keys = new ArrayList<>(labels.keySet());

        // 防止相同map因排序不同而导致最后的label不一致
        Collections.sort(keys);
        for (String key : keys) {
            String value = labels.get(key);
            if (key == null || value == null) {
                LOGGER.warn(String.format(Locale.ENGLISH, "Invalid group label, key = %s, value = %s",
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
     * 重组group, 防止因多个标签因顺序问题而导致group不同
     *
     * @param group 标签组
     * @return group
     */
    public static String rebuildGroup(String group) {
        if (isLabelGroup(group)) {
            return createLabelGroup(resolveGroupLabels(group));
        }
        return LabelGroupUtils.createLabelGroup(Collections.singletonMap("GROUP", group));
    }

    /**
     * 是否为标签组key
     *
     * @param group 监听键
     * @return 是否为标签组
     */
    public static boolean isLabelGroup(String group) {
        return group != null && group.contains(KV_SEPARATOR);
    }

    /**
     * 解析标签为map
     *
     * @param group 标签组  app=sc&service=helloService
     * @return 标签键值对
     */
    public static Map<String, String> resolveGroupLabels(String group) {
        final Map<String, String> result = new HashMap<>();
        if (group == null) {
            return result;
        }
        String curGroup = group;
        if (!isLabelGroup(curGroup)) {
            // 如果非group标签（ZK配置中心场景适配），则为该group创建标签
            curGroup = LabelGroupUtils.createLabelGroup(Collections.singletonMap("GROUP", curGroup));
        }
        try {
            final String decode = URLDecoder.decode(curGroup, "UTF-8");
            final String[] labels = decode.split("&");
            for (String label : labels) {
                final String[] labelKv = label.split("=");
                if (labelKv.length != KV_LEN) {
                    continue;
                }
                result.put(labelKv[0], labelKv[1]);
            }
        } catch (UnsupportedEncodingException ignored) {
            // ignored
        }
        return result;
    }

    /**
     * 获取标签信息
     *
     * @param group 分组  app=sc&service=helloService转换label=app:sc&label=service:helloService
     * @return 标签组条件
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
        } catch (UnsupportedEncodingException ignored) {
            // ignored
        }
        return StringUtils.EMPTY;
    }
}
