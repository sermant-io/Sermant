/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.addr;

import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.route.common.factory.NamedThreadFactory;
import com.huawei.route.common.gray.addr.entity.Addr;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.utils.CollectionUtils;
import com.huawei.route.common.utils.HttpClientResult;
import com.huawei.route.common.utils.HttpClientUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 地址缓存
 *
 * @author pengyuyi
 * @date 2021/10/15
 */
public class AddrCache {
    private static final Logger LOGGER = LogFactory.getLogger();

    // 需要刷新地址的缓存
    private static final Map<String, List<Instances>> CACHE = new ConcurrentHashMap<String, List<Instances>>();

    // 定时执行的线程池
    private static ScheduledExecutorService executorService;

    /**
     * 获取路由服务返回的地址列表
     *
     * @param application 服务名
     * @return 实例列表
     */
    public static Map<String, List<Instances>> getAddr(String application) {
        List<Instances> instanceList = getAddrFromCache(application);
        if (CollectionUtils.isEmpty(instanceList)) {
            return Collections.emptyMap();
        }
        Map<String, List<Instances>> resultMap = new HashMap<String, List<Instances>>();
        for (Instances instance : instanceList) {
            if (instance.getCurrentTag() == null) {
                continue;
            }
            String version = instance.getCurrentTag().getVersion();
            if (resultMap.containsKey(version)) {
                resultMap.get(version).add(instance);
            } else {
                List<Instances> resultList = new ArrayList<Instances>();
                resultList.add(instance);
                resultMap.put(version, resultList);
            }
        }
        return resultMap;
    }

    /**
     * 获取路由服务返回的实例
     *
     * @param application 服务名
     * @param ldc ldc
     * @param version 版本号
     * @return 实例
     */
    public static Instances getAddr(String application, String ldc, String version) {
        if (ldc == null || version == null) {
            return null;
        }
        List<Instances> instanceList = getAddrFromCache(application);
        if (CollectionUtils.isEmpty(instanceList)) {
            return null;
        }
        Instances[] arr = new Instances[2];
        for (Instances instance : instanceList) {
            CurrentTag currentTag = instance.getCurrentTag();
            if (currentTag == null) {
                continue;
            }
            String instanceLdc = instance.getLdc();
            String instanceVersion = currentTag.getVersion();
            if (ldc.equals(instanceLdc) && version.equals(instanceVersion)) {
                // 优先返回这个实例
                return instance;
            }
            if (!ldc.equals(instanceLdc) && version.equals(instanceVersion)) {
                // 第二优先级
                arr[0] = instance;
                continue;
            }
            if (ldc.equals(instanceLdc)) {
                // 第三优先级
                arr[1] = instance;
            }
            // 这里的优先级可考虑优化成分数的形式，然后取分数最高的
        }
        if (arr[0] != null) {
            return arr[0];
        }
        if (arr[1] != null) {
            return arr[1];
        }
        return null;
    }

    /**
     * 根据应用名，host获取实例
     *
     * @param application 应用名
     * @param host host
     * @return 实例
     */
    public static Instances getInstance(String application, String host) {
        List<Instances> instanceList = getAddrFromCache(application);
        if (CollectionUtils.isEmpty(instanceList)) {
            return null;
        }
        for (Instances instance : instanceList) {
            if ((instance.getIp() + ":" + instance.getPort()).equals(host)) {
                return instance;
            }
        }
        return null;
    }

    private static List<Instances> getAddrFromCache(String application) {
        List<Instances> instanceList = CACHE.get(application);
        if (CollectionUtils.isEmpty(instanceList)) {
            CACHE.put(application, new ArrayList<Instances>());
            refreshCacheTask();
            instanceList = CACHE.get(application);
        }
        return instanceList;
    }

    /**
     * 开启刷新地址定时任务
     */
    public static void start() {
        shutdown(false);
        executorService = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory(GrayConstant.QUERY_SERVICE_ADDR_THREAD_NAME));
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshCacheTask();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 关闭定时任务
     *
     * @param clear 是否需要清除需要查询地址的集合
     */
    public static void shutdown(boolean clear) {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        if (clear) {
            CACHE.clear();
        }
    }

    // 这里主动查和定时查有可能会同时触发，所以用synchronized
    private static synchronized void refreshCacheTask() {
        JSONObject json = new JSONObject();
        json.put(GrayConstant.QUERY_SERVICE_ADDR_KEY, CACHE.keySet());
        try {
            HttpClientResult httpClientResult = HttpClientUtils
                    .doPost(ConfigLoader.getConfig(GrayConfig.class).getQueryInstanceAddrUrl(), json.toJSONString());
            setCache(JSONArray.parseArray(httpClientResult.getContent(), Addr.class));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Route server is error.", e);
        }
    }

    private static void setCache(List<Addr> addrList) {
        if (CollectionUtils.isEmpty(addrList)) {
            return;
        }
        CACHE.clear();
        for (Addr addr : addrList) {
            if (Addr.isEmpty(addr)) {
                continue;
            }
            CACHE.put(addr.getServiceName(), new ArrayList<Instances>(addr.getInstances()));
        }
    }
}
