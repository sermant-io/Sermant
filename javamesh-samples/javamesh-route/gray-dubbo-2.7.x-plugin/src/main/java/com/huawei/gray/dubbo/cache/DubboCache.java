/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.cache;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
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

    // 本地路由的参数缓存
    private static final Map<String, Map<String, String>> LOCAL_PARAMETER_CACHE =
            new ConcurrentHashMap<String, Map<String, String>>();

    // dubbo应用名
    private static String APP_NAME;

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
        return addrs.toArray(addrs.toArray(new String[0]))[0];
    }

    /**
     * 获取本地路由地址列表
     *
     * @param application 服务名
     * @return host列表
     */
    public static Set<String> getLocalAddrList(String application) {
        Set<String> addrs = LOCAL_CACHE.get(application);
        if (CollectionUtils.isEmpty(addrs)) {
            return Collections.emptySet();
        }
        return addrs;
    }

    /**
     * 全量更新本地路由地址列表
     *
     * @param map 本地路由地址Map
     */
    public static void updateLocalAddr(Map<String, Set<String>> map) {
        LOCAL_CACHE.clear();
        LOCAL_CACHE.putAll(map);
    }

    /**
     * 全量更新本地路由地址列表
     *
     * @param map 本地路由地址Map
     */
    public static void updateLocalParameters(Map<String, Map<String, String>> map) {
        LOCAL_PARAMETER_CACHE.clear();
        LOCAL_PARAMETER_CACHE.putAll(map);
    }

    /**
     * 全量更新本地路由地址列表
     *
     * @param host 本地路由地址Map
     * @return 本地路由地址url中的Parameters
     */
    public static Map<String, String> getLocalParameters(String host) {
        return LOCAL_PARAMETER_CACHE.get(host);
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

}
