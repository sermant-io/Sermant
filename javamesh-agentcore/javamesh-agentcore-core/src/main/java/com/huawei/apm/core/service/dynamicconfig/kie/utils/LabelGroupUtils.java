/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.utils;

import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieRequestFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * group生成工具
 *
 * @author zhouss
 * @since 2021-11-23
 */
public class LabelGroupUtils {
    private LabelGroupUtils(){
    }

    private static final String LABEL_FLAG = "LABEL-FLAG";

    private static final String LABEL_SEPARATOR = "#";

    /**
     * 创建标签组
     *
     * @param labels 标签组
     * @return labelGroup 例如: label=app%3Asc&label=version%3A1.0&#10
     */
    public static String createLabelGroup(Map<String, String> labels) {
        return LABEL_FLAG + LABEL_SEPARATOR + KieRequestFactory.buildMapLabels(labels);
    }

    /**
     * 是否为标签组key
     *
     * @param group 监听键
     * @return 是否为标签组
     */
    public static boolean isLabelGroup(String group) {
        return group != null && group.startsWith(LABEL_FLAG + LABEL_SEPARATOR);
    }

    /**
     * 解析标签为map
     *
     * @param group 标签组
     * @return 标签键值对
     */
    public static Map<String, String> resolveGroupLabels(String group) {
        final Map<String, String> result = new HashMap<>();
        if (group == null) {
            return result;
        }
        if (!isLabelGroup(group)) {
            return result;
        }
        String labelCondition = getLabelCondition(group);
        try {
            final String decode = URLDecoder.decode(labelCondition, "UTF-8");
            final String[] labels = decode.split("&");
            for (String label : labels) {
                final String[] parts = label.split("=");
                if (parts.length != 2) {
                    continue;
                }
                final String[] labelKv = parts[1].split(":");
                if (labelKv.length != 2) {
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
     * @param group 分组
     * @return 标签组条件
     */
    public static String getLabelCondition(String group) {
        if (group == null || group.length() < LABEL_FLAG.length() + 1) {
            return null;
        }
        return  group.substring(LABEL_FLAG.length() + 1, group.length() - 1);
    }
}
