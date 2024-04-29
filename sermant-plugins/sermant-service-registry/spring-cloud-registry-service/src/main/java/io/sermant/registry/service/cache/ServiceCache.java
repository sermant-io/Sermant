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

package io.sermant.registry.service.cache;

import io.sermant.registry.service.register.NacosServiceInstance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service information caching
 *
 * @since 2022-10-20
 */
public final class ServiceCache {
    private static List<String> services = Collections.emptyList();

    private static Map<String, List<NacosServiceInstance>> instancesMap = new ConcurrentHashMap<>();

    private ServiceCache() {
    }

    /**
     * Configure cache instance information
     *
     * @param serviceId Service ID
     * @param instances A collection of instances
     */
    public static void setInstances(String serviceId, List<NacosServiceInstance> instances) {
        instancesMap.put(serviceId, Collections.unmodifiableList(instances));
    }

    /**
     * Get a collection of service names
     *
     * @return A collection of service names
     */
    public static List<String> getServiceIds() {
        return services;
    }

    /**
     * Obtain the caching service information
     *
     * @param serviceId Service ID
     * @return Service collection information
     */
    public static List<NacosServiceInstance> getInstances(String serviceId) {
        return instancesMap.getOrDefault(serviceId, Collections.emptyList());
    }

    /**
     * Set up a collection of service names
     *
     * @param serviceIds A collection of service IDs
     */
    public static void setServiceIds(List<String> serviceIds) {
        services = Collections.unmodifiableList(serviceIds);
    }
}
