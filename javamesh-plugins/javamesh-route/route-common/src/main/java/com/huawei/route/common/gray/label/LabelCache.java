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
     * 获取指定标签
     *
     * @param labelName 缓存的标签名
     * @return 标签
     */
    public static GrayConfiguration getLabel(String labelName) {
        GrayConfiguration grayConfiguration = CACHE.get(labelName);
        if (grayConfiguration == null) {
            synchronized (LabelCache.class) {
                grayConfiguration = CACHE.get(labelName);
                if (grayConfiguration == null) {
                    CACHE.put(labelName, new GrayConfiguration());
                    grayConfiguration = CACHE.get(labelName);
                }
            }
        }
        return grayConfiguration;
    }
}