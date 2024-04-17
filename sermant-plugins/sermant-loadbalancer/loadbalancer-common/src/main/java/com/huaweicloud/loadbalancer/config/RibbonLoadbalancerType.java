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
 * ribbon load balance strategy
 *
 * @author provenceee
 * @since 2022-01-21
 */
public enum RibbonLoadbalancerType {
    /**
     * Random
     */
    RANDOM("Random", "com.netflix.loadbalancer.RandomRule"),

    /**
     * Round Robin
     */
    ROUND_ROBIN("RoundRobin", "com.netflix.loadbalancer.RoundRobinRule"),

    /**
     * Retry
     */
    RETRY("Retry", "com.netflix.loadbalancer.RetryRule"),

    /**
     * Best Available
     */
    BEST_AVAILABLE("BestAvailable", "com.netflix.loadbalancer.BestAvailableRule"),

    /**
     * Availability Filtering
     */
    AVAILABILITY_FILTERING("AvailabilityFiltering", "com.netflix.loadbalancer.AvailabilityFilteringRule"),

    /**
     * Response Time Weighted
     *
     * @deprecated Use {@link #WEIGHTED_RESPONSE_TIME} instead.
     */
    @Deprecated
    RESPONSE_TIME_WEIGHTED("ResponseTimeWeighted", "com.netflix.loadbalancer.ResponseTimeWeightedRule"),

    /**
     * Zone Avoidance
     */
    ZONE_AVOIDANCE("ZoneAvoidance", "com.netflix.loadbalancer.ZoneAvoidanceRule"),

    /**
     * Weighted Response Time
     */
    WEIGHTED_RESPONSE_TIME("WeightedResponseTime", "com.netflix.loadbalancer.WeightedResponseTimeRule");

    /**
     * name of the actual mapping
     */
    private final String mapperName;

    /**
     * rule class name
     */
    private final String clazzName;

    RibbonLoadbalancerType(String mapperName, String clazzName) {
        this.mapperName = mapperName;
        this.clazzName = clazzName;
    }

    /**
     * match the load balancing type
     *
     * @param loadbalancerType loadbalancer type
     * @return loadbalancer type
     */
    public static Optional<RibbonLoadbalancerType> matchLoadbalancer(String loadbalancerType) {
        if (loadbalancerType == null) {
            return Optional.empty();
        }
        for (RibbonLoadbalancerType type : values()) {
            if (type.mapperName.equalsIgnoreCase(loadbalancerType)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * match the load balancing type
     *
     * @param clazzName load balancing rule class name
     * @return loadbalancer type
     */
    public static Optional<RibbonLoadbalancerType> matchLoadbalancerByClazz(String clazzName) {
        if (clazzName == null) {
            return Optional.empty();
        }
        for (RibbonLoadbalancerType type : values()) {
            if (type.clazzName.equalsIgnoreCase(clazzName)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getMapperName() {
        return mapperName;
    }
}
