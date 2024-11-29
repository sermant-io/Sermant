/*
 * Copyright (C) 2022-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.utils;

import com.netflix.loadbalancer.Server;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.cache.AppCache;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ReflectUtils;

import org.springframework.cloud.client.DefaultServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reflection tool class
 *
 * @author provenceee
 * @since 2022-07-19
 */
public class SpringRouterUtils {
    private static final String VERSION_KEY = "version";

    private static final String ZONE_KEY = "zone";

    private static final String QUERY_SEPARATOR = "&";

    private static final String KV_SEPARATOR = "=";

    private static final int KV_SPLIT_LENGTH = 2;

    private static RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    private SpringRouterUtils() {
    }

    /**
     * get parameters from query sting
     *
     * @param query query sting
     * @return parameters
     */
    public static Map<String, List<String>> getParametersByQuery(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        String[] queryArr = query.split(QUERY_SEPARATOR);
        Map<String, List<String>> parameters = new HashMap<>();
        for (String kv : queryArr) {
            String[] kvArr = kv.split(KV_SEPARATOR, KV_SPLIT_LENGTH);
            parameters.computeIfAbsent(kvArr[0], value -> new ArrayList<>()).add(kvArr[1]);
        }
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * get http query
     *
     * @param obj HttpServletRequest
     * @return query
     */
    public static String getQueryString(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getQueryString");
    }

    /**
     * get http uri
     *
     * @param obj HttpServletRequest
     * @return uri
     */
    public static String getRequestUri(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getRequestURI");
    }

    /**
     * get http method
     *
     * @param obj HttpServletRequest
     * @return method
     */
    public static String getMethod(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, "getMethod");
    }

    /**
     * get http header keys
     *
     * @param obj HttpServletRequest
     * @return key
     */
    public static Enumeration<?> getHeaderNames(Object obj) {
        return (Enumeration<?>) ReflectUtils.invokeWithNoneParameter(obj, "getHeaderNames");
    }

    /**
     * get http header value
     *
     * @param obj HttpServletRequest
     * @param key header key
     * @return header value
     */
    public static Enumeration<?> getHeaders(Object obj, String key) {
        return (Enumeration<?>) ReflectUtils.invokeWithParameter(obj, "getHeaders", key, String.class);
    }

    /**
     * get SpringCloud ServiceInstance By XdsServiceInstance
     *
     * @param xdsServiceInstances
     * @return spring cloud service instance
     */
    public static List<org.springframework.cloud.client.ServiceInstance> getSpringCloudServiceInstanceByXds(
            Set<ServiceInstance> xdsServiceInstances) {
        List<org.springframework.cloud.client.ServiceInstance> serviceInstances = new ArrayList<>();
        for (ServiceInstance xdsServiceInstance : xdsServiceInstances) {
            serviceInstances.add(convertServiceInstance(xdsServiceInstance));
        }
        return serviceInstances;
    }

    /**
     * get SpringCloud ServiceInstance By XdsServiceInstance
     *
     * @param xdsServiceInstances
     * @return spring cloud service instance
     */
    public static List<Server> getSpringCloudServerByXds(Set<ServiceInstance> xdsServiceInstances) {
        List<Server> servers = new ArrayList<>();
        for (ServiceInstance xdsServiceInstance : xdsServiceInstances) {
            servers.add(convertServiceInstance2Server(xdsServiceInstance));
        }
        return servers;
    }

    /**
     * Get metadata
     *
     * @param obj Object
     * @return Metadata
     */
    public static Map<String, String> getMetadata(Object obj) {
        return (Map<String, String>) ReflectUtils.invokeWithNoneParameter(obj, "getMetadata");
    }

    /**
     * Deposit metadata
     *
     * @param metadata Metadata
     * @param config Route configuration
     */
    public static void putMetaData(Map<String, String> metadata, RouterConfig config) {
        if (metadata == null) {
            return;
        }
        metadata.putIfAbsent(VERSION_KEY, config.getRouterVersion());
        if (StringUtils.isExist(config.getZone())) {
            metadata.putIfAbsent(ZONE_KEY, config.getZone());
        }
        Map<String, String> parameters = config.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            // The request header is changed to lowercase in the HTTP request
            parameters.forEach((key, value) -> metadata.putIfAbsent(key.toLowerCase(Locale.ROOT), value));
        }
        AppCache.INSTANCE.setMetadata(metadata);
    }

    private static org.springframework.cloud.client.ServiceInstance convertServiceInstance(
            ServiceInstance xdsServiceInstance) {
        StringBuilder instanceIdBuilder = new StringBuilder();
        instanceIdBuilder.append(xdsServiceInstance.getHost())
                .append(":")
                .append(xdsServiceInstance.getPort());
        return new DefaultServiceInstance(
                instanceIdBuilder.toString(), xdsServiceInstance.getServiceName(), xdsServiceInstance.getHost(),
                xdsServiceInstance.getPort(), routerConfig.isEnabledSpringCloudXdsRouteSecure());
    }

    private static Server convertServiceInstance2Server(ServiceInstance xdsServiceInstance) {
        String scheme = routerConfig.isEnabledSpringCloudXdsRouteSecure() ? "https" : "http";
        return new Server(scheme, xdsServiceInstance.getHost(), xdsServiceInstance.getPort());
    }
}
