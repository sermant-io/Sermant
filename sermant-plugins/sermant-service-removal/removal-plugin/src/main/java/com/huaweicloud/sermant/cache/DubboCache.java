/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo服务名称和接口映射缓存类
 *
 * @author zhp
 * @since 2023-03-17
 */
public class DubboCache {
    /**
     * 缓存MAP
     */
    private static final Map<String, String> SERVICE_CACHE = new HashMap<>();

    private DubboCache() {
    }

    /**
     * 缓存接口与服务名的关系
     *
     * @param interfaceName 接口名
     * @param serviceName 服务名
     */
    public static void putApplication(String interfaceName, String serviceName) {
        SERVICE_CACHE.put(interfaceName, serviceName);
    }

    /**
     * 获取应用名
     *
     * @param serviceInterface 接口
     * @return 应用名
     */
    public static String getServiceName(String serviceInterface) {
        return SERVICE_CACHE.get(serviceInterface);
    }
}
