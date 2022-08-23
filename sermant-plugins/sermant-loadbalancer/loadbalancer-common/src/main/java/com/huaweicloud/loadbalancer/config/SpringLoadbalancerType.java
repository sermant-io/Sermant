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

package com.huaweicloud.loadbalancer.config;

import java.util.Optional;

/**
 * spring cloud loadbalancer负载均衡策略
 *
 * @author provenceee
 * @since 2022-01-21
 */
public enum SpringLoadbalancerType {
    /**
     * 随机（支持spring cloud loadbalancer 2.2.7+）
     */
    RANDOM("Random"),

    /**
     * 轮询
     */
    ROUND_ROBIN("RoundRobin");

    /**
     * 实际配置映射名称
     */
    private final String mapperName;

    SpringLoadbalancerType(String mapperName) {
        this.mapperName = mapperName;
    }

    /**
     * 匹配负载均衡类型
     *
     * @param loadbalancerType 负载均衡
     * @return 负载均衡类型
     */
    public static Optional<SpringLoadbalancerType> matchLoadbalancer(String loadbalancerType) {
        if (loadbalancerType == null) {
            return Optional.empty();
        }
        for (SpringLoadbalancerType type : values()) {
            if (type.mapperName.equalsIgnoreCase(loadbalancerType)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public String getMapperName() {
        return mapperName;
    }
}
