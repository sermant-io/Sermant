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

package com.huawei.route.common.gray.addr;

import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地址缓存
 *
 * @author provenceee
 * @since 2021/10/15
 */
public class AddrCache {
    // 需要刷新地址的缓存
    private static final Map<String, List<Instances>> CACHE = new ConcurrentHashMap<String, List<Instances>>();

    // 注册版本缓存
    private static final Map<String, String> REGISTER_VERSION_CACHE = new ConcurrentHashMap<String, String>();

    private AddrCache() {
    }

    /**
     * 获取路由服务返回的地址列表
     *
     * @param targetService 目标服务名
     * @param labelName 缓存的标签名
     * @return 实例列表
     */
    public static Map<String, List<Instances>> getAddr(String targetService, String labelName) {
        List<Instances> instanceList = getAddrFromCache(targetService);
        if (CollectionUtils.isEmpty(instanceList)) {
            return Collections.emptyMap();
        }
        Map<String, List<Instances>> resultMap = new HashMap<String, List<Instances>>();
        for (Instances instance : instanceList) {
            CurrentTag currentTag = instance.getCurrentTag();
            if (currentTag == null) {
                continue;
            }
            String version = labelName == null ? currentTag.getVersion() : currentTag.getValidVersion(labelName);
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
     * @param targetService 服务名
     * @param application 应用服务名
     * @param ldc ldc
     * @param version 版本号
     * @return 实例
     */
    public static Instances getAddr(String targetService, String ldc, String version, String application) {
        if (ldc == null || version == null) {
            return null;
        }
        List<Instances> instanceList = getAddrFromCache(targetService);
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
            String instanceVersion =
                application == null ? currentTag.getVersion() : currentTag.getValidVersion(application);
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
            return Collections.emptyList();
        }
        return instanceList;
    }

    /**
     * 刷新地址缓存
     *
     * @param map 地址列表
     */
    public static void setCache(Map<String, List<Instances>> map) {
        CACHE.putAll(map);
    }

    /**
     * 设置注册版本缓存
     *
     * @param addr 地址
     * @param version 注册版本
     */
    public static void setRegisterVersionCache(String addr, String version) {
        REGISTER_VERSION_CACHE.put(addr, version);
    }

    /**
     * 获取注册版本缓存
     *
     * @param addr 地址
     * @return 注册版本
     */
    public static String getRegisterVersionCache(String addr) {
        return REGISTER_VERSION_CACHE.get(addr);
    }
}
