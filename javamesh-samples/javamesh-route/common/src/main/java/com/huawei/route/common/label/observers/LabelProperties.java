/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.label.observers;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 标签配置
 *
 * @author zhanghu
 * @since 2021-06-09
 */
public class LabelProperties {
    /**
     * 存放所有的标签信息，key为标签名，value为传入的所有信息，
     * value的属性中包含字段为‘value’的字符串，该值为标签的具体信息
     */
    private static final Map<String, Properties> ALL_LABEL_PROPERTIES = new ConcurrentHashMap<String, Properties>();

    private LabelProperties() {
    }

    /**
     * 获取所有标签信息
     *
     * @return 标签集合
     */
    public static Map<String, Properties> getAllLabelProperties() {
        return ALL_LABEL_PROPERTIES;
    }
}
