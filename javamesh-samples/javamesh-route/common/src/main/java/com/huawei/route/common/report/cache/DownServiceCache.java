/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.cache;

import java.util.HashSet;
import java.util.Set;

/**
 * 下游应用名称的缓存
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class DownServiceCache {
    private final Set<String> DOWN_SERVICE_SET = new HashSet<String>();

    private static final DownServiceCache DOWN_SERVICE_CACHE = new DownServiceCache();

    public static DownServiceCache getInstance() {
        return DOWN_SERVICE_CACHE;
    }

    /**
     * 添加下游应用名称
     *
     * @param downServiceName 下游应用名称
     * @return 添加结果
     */
    public boolean addDownService(String downServiceName) {
        return DOWN_SERVICE_SET.add(downServiceName);
    }

    /**
     * 判断下游应用是否存在集合内
     *
     * @param downServiceName 下游应用名称
     * @return 判断结果
     */
    public boolean downServiceExits(String downServiceName) {
        return DOWN_SERVICE_SET.contains(downServiceName);
    }
}
