/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.cache;

import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dubbo缓存
 *
 * @author pengyuyi
 * @date 2021/11/3
 */
public class DubboCache {
    // 本地路由缓存
    private static final Map<String, Set<String>> LOCAL_CACHE = new ConcurrentHashMap<String, Set<String>>();

    // dubbo应用名
    private static String APP_NAME;

    // dubbo应用灰度标签缓存名
    private static final String GRAY_LABEL_CACHE_NAME = "DUBBO_GRAY_LABEL";

    /**
     * 获取本地路由地址
     *
     * @param application 服务名
     * @return host
     */
    public static String getLocalAddr(String application) {
        Set<String> addrs = LOCAL_CACHE.get(application);
        if (CollectionUtils.isEmpty(addrs)) {
            return null;
        }
        return addrs.toArray(addrs.toArray(new String[0]))[new Random().nextInt(addrs.size())];
    }

    /**
     * 全量更新本地路由地址列表
     *
     * @param map 本地路由地址Map
     */
    public static void updateLocalAddr(Map<String, Set<String>> map) {
        LOCAL_CACHE.putAll(map);
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    /**
     * 获取dubbo应用灰度标签缓存名
     *
     * @return dubbo应用灰度标签缓存名
     */
    public static String getLabelName() {
        return GRAY_LABEL_CACHE_NAME;
    }
}
