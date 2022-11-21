/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.service.cache;

import com.huawei.registry.service.register.NacosServiceInstance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务信息缓存
 *
 * @since 2022-10-20
 */
public final class ServiceCache {
    private static List<String> services = Collections.emptyList();

    private static Map<String, List<NacosServiceInstance>> instancesMap = new ConcurrentHashMap<>();

    private ServiceCache() {
    }

    /**
     * 设置缓存实例信息
     *
     * @param serviceId 服务id
     * @param instances 实例集合
     */
    public static void setInstances(String serviceId, List<NacosServiceInstance> instances) {
        instancesMap.put(serviceId, Collections.unmodifiableList(instances));
    }

    /**
     * 获取服务名称集合
     *
     * @return 服务名集合
     */
    public static List<String> getServiceIds() {
        return services;
    }

    /**
     * 获取缓存服务信息
     *
     * @param serviceId 服务id
     * @return 服务集合信息
     */
    public static List<NacosServiceInstance> getInstances(String serviceId) {
        return instancesMap.getOrDefault(serviceId, Collections.emptyList());
    }

    /**
     * 设置服务名集合
     *
     * @param serviceIds 服务id集合
     */
    public static void setServiceIds(List<String> serviceIds) {
        services = Collections.unmodifiableList(serviceIds);
    }
}
