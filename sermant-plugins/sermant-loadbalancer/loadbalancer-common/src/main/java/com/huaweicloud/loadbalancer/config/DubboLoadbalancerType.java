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
 * dubbo load balance strategy
 *
 * @author provenceee
 * @since 2022-01-21
 */
public enum DubboLoadbalancerType {
    /**
     * Random
     */
    RANDOM("Random"),

    /**
     * Round Robin
     */
    ROUNDROBIN("RoundRobin"),

    /**
     * least active
     */
    LEASTACTIVE("LeastActive"),

    /**
     * Consistent Hash
     */
    CONSISTENTHASH("ConsistEntHash"),

    /**
     * shortest response（only dubbo2.7.7+ is supported）
     */
    SHORTESTRESPONSE("ShortestResponse");

    /**
     * name of the actual mapping
     */
    private final String mapperName;

    DubboLoadbalancerType(String mapperName) {
        this.mapperName = mapperName;
    }

    /**
     * match the load balancing type
     *
     * @param loadbalancerType loadbalancer type
     * @return loadbalancerType
     */
    public static Optional<DubboLoadbalancerType> matchLoadbalancer(String loadbalancerType) {
        if (loadbalancerType == null) {
            return Optional.empty();
        }
        for (DubboLoadbalancerType type : values()) {
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
