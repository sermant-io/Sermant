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

package com.huawei.registry.utils;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 刷新缓存工具类
 *
 * @author provenceee
 * @since 2022-05-27
 */
public class RefreshUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RefreshUtils() {
    }

    /**
     * 刷新目标服务实例缓存
     *
     * @param serviceName 下游服务名
     */
    public static void refreshTargetServiceInstances(String serviceName) {
        refreshTargetServiceInstances(serviceName, null);
    }

    /**
     * 刷新目标服务实例缓存
     *
     * @param serviceName 下游服务名
     * @param responseServiceNames 响应标记下线的服务名, 仅一个
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
     * 获取Ribbon负载均衡
     *
     * @param serviceName 下游服务名
     * @param responseServiceNames 响应标记下线的服务名, 仅一个
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
        LOGGER.fine(String.format(Locale.ENGLISH,
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
        LOGGER.fine(String.format(Locale.ENGLISH,
            "Start refresh target service [%s] ribbon instance cache!", serviceName.orElse("unKnow service")));
        ReflectUtils.invokeMethod(loadbalancer, "updateListOfServers", null, null);
    }
}
