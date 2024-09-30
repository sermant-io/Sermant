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

package io.sermant.registry.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Refresh the cache utility class
 *
 * @author provenceee
 * @since 2022-05-27
 */
public class RefreshUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RefreshUtils() {
    }

    /**
     * Refresh the cache of the target service instance
     *
     * @param serviceName Downstream service name
     */
    public static void refreshTargetServiceInstances(String serviceName) {
        refreshTargetServiceInstances(serviceName, null);
    }

    /**
     * Refresh the cache of the target service instance
     *
     * @param serviceName Downstream service name
     * @param responseServiceNames The response marks the service name offline, only one
     */
    public static void refreshTargetServiceInstances(String serviceName, Collection<String> responseServiceNames) {
        final Object ribbonLoadbalancer = getRibbonLoadbalancer(serviceName, responseServiceNames);
        if (ribbonLoadbalancer == null) {
            refreshWithSpringLb(serviceName, responseServiceNames);
        } else {
            refreshWithRibbon(ribbonLoadbalancer);
        }
    }

    /**
     * Obtain the Ribbon load balancer
     *
     * @param serviceName Downstream service name
     * @param responseServiceNames The response marks the service name offline, only one
     * @return loadbalancer
     */
    private static Object getRibbonLoadbalancer(String serviceName, Collection<String> responseServiceNames) {
        Object result = null;
        if (serviceName != null) {
            result = GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCache()
                    .get(serviceName);
        }
        if (result == null) {
            if (responseServiceNames != null && !responseServiceNames.isEmpty()) {
                result = GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCache()
                        .get(responseServiceNames.iterator().next());
            }
        }
        return result;
    }

    private static void refreshWithSpringLb(String serviceName, Collection<String> responseServiceNames) {
        final Object loadBalancerCacheManager = GraceContext.INSTANCE.getGraceShutDownManager()
                .getLoadBalancerCacheManager();
        String curServiceName = serviceName;
        if (curServiceName == null && responseServiceNames != null && !responseServiceNames.isEmpty()) {
            curServiceName = responseServiceNames.iterator().next();
        }
        if (loadBalancerCacheManager == null) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Can not refresh service [%s] instance cache with spring loadbalancer!", curServiceName));
            return;
        }
        LOGGER.info(String.format(Locale.ENGLISH,
                "Start refresh target service [%s] spring loadbalancer instance cache!", curServiceName));
        final Optional<Object> cacheOptional = ReflectUtils.invokeMethod(loadBalancerCacheManager, "getCache",
                new Class[]{String.class}, new Object[]{GraceConstants.SPRING_CACHE_MANAGER_LOADBALANCER_CACHE_NAME});
        if (cacheOptional.isPresent()) {
            final Object cache = cacheOptional.get();
            ReflectUtils.invokeMethod(cache, "evict", new Class[]{Object.class}, new Object[]{curServiceName});
            if (responseServiceNames != null && !responseServiceNames.isEmpty()) {
                ReflectUtils.invokeMethod(cache, "evict", new Class[]{Object.class},
                        new Object[]{responseServiceNames.iterator().next()});
            }
        }
    }

    private static void refreshWithRibbon(Object loadbalancer) {
        final Optional<Object> serviceName = ReflectUtils.getFieldValue(loadbalancer, "name");
        LOGGER.info(String.format(Locale.ENGLISH,
                "Start refresh target service [%s] ribbon instance cache!", serviceName.orElse("unKnow service")));
        ReflectUtils.invokeMethod(loadbalancer, "updateListOfServers", null, null);
    }
}
