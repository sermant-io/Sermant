/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.utils;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * nacos实例管理工具
 *
 * @since 2022-10-25
 */
public class NacosInstanceManageUtil {
    private static final Map<String, List<Instance>> SERVICE_INSTANCE_LIST_MAP = Maps.newConcurrentMap();

    private static final Map<String, Set<String>> CORRESPONDING_SERVICE_NAMES_MAP = Maps.newConcurrentMap();

    private NacosInstanceManageUtil() {
    }

    /**
     * 设置服务名
     *
     * @param serviceName 服务名
     * @param serviceNames 服务名集合
     */
    public static void setCorrespondingServiceNames(String serviceName, Set<String> serviceNames) {
        CORRESPONDING_SERVICE_NAMES_MAP.put(serviceName, serviceNames);
    }

    /**
     * 实例化、更新服务实例信息
     *
     * @param serviceName 服务名
     * @param instances 实例集合
     */
    public static void initOrRefreshServiceInstanceList(String serviceName, List<Instance> instances) {
        SERVICE_INSTANCE_LIST_MAP.put(serviceName, instances);
    }

    /**
     * 获取实例
     *
     * @param serviceName 服务名
     * @return 实例集合
     */
    public static List<Instance> getAllCorrespondingServiceInstanceList(String serviceName) {
        if (!CORRESPONDING_SERVICE_NAMES_MAP.containsKey(serviceName)) {
            return Lists.newArrayList();
        }
        List<Instance> allInstances = Lists.newArrayList();
        for (String correspondingServiceName : CORRESPONDING_SERVICE_NAMES_MAP.get(serviceName)) {
            if (SERVICE_INSTANCE_LIST_MAP.containsKey(correspondingServiceName)
                    && !CollectionUtils.isEmpty(SERVICE_INSTANCE_LIST_MAP.get(correspondingServiceName))) {
                allInstances.addAll(SERVICE_INSTANCE_LIST_MAP.get(correspondingServiceName));
            }
        }
        return allInstances;
    }

    /**
     * 删除服务名对应实例
     *
     * @param serviceName 服务名
     */
    public static void removeCorrespondingServiceNames(String serviceName) {
        CORRESPONDING_SERVICE_NAMES_MAP.remove(serviceName);
    }
}
