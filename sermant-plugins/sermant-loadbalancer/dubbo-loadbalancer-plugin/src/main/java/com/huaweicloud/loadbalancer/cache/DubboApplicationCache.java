/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.loadbalancer.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dubbo服务名缓存
 *
 * @author zhouss
 * @since 2022-09-13
 */
public enum DubboApplicationCache {
    /**
     * 实例
     */
    INSTANCE;

    /**
     * 服务接口缓存 key:接口名  value:下游服务名
     */
    private final Map<String, String> applicationCache = new ConcurrentHashMap<>();

    /**
     * 缓存
     *
     * @param interfaceName 接口名
     * @param application 服务名
     */
    public void cache(String interfaceName, String application) {
        applicationCache.put(interfaceName, application);
    }

    public Map<String, String> getApplicationCache() {
        return applicationCache;
    }
}
