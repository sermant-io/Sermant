/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.utils;

import java.util.Optional;

/**
 * ribbon负载均衡工具类
 *
 * @author zhouss
 * @since 2022-08-12
 */
public class RibbonUtils {
    /**
     * 默认的ribbon负载均衡键
     */
    public static final String DEFAULT_RIBBON_LOADBALANCER_KEY = "default";

    /**
     * 负载均衡key前缀, 防止与用户的负载均衡键重合
     */
    public static final String RIBBON_LOADBALANCER_KEY_PREFIX = "__SERMANT_LOADBALANCER__";

    private static final char RIBBON_KEY_SEPARATOR = '#';

    /**
     * 构建负载均衡key
     *
     * @param serviceName 服务名
     * @return key
     */
    public static String buildLoadbalancerKey(String serviceName) {
        return RIBBON_LOADBALANCER_KEY_PREFIX + RIBBON_KEY_SEPARATOR + serviceName;
    }

    /**
     * 通过负载均衡key解析服务名
     *
     * @param loadbalancerKey 负载均衡key
     * @return serviceName
     */
    public static Optional<String> resolveServiceNameByKey(String loadbalancerKey) {
        if (loadbalancerKey == null || !loadbalancerKey.startsWith(RIBBON_LOADBALANCER_KEY_PREFIX)) {
            return Optional.empty();
        }
        final int index = loadbalancerKey.lastIndexOf(RIBBON_KEY_SEPARATOR);
        if (index != -1) {
            return Optional.of(loadbalancerKey.substring(index + 1));
        }
        return Optional.empty();
    }
}
