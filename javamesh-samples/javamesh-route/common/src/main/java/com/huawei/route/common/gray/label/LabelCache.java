/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label;

import com.huawei.route.common.gray.label.entity.GrayConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 标签缓存
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class LabelCache {
    // 需要刷新标签的缓存
    private static final Map<String, GrayConfiguration> CACHE = new ConcurrentHashMap<String, GrayConfiguration>();

    /**
     * 设置标签
     *
     * @param application 应用名
     * @param grayConfiguration 标签
     */
    public static void setLabel(String application, GrayConfiguration grayConfiguration) {
        CACHE.put(application, grayConfiguration);
    }

    /**
     * 获取指定服务标签
     *
     * @param application 应用名
     * @return 标签
     */
    public static GrayConfiguration getLabel(String application) {
        return CACHE.get(application);
    }
}