/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol.common.util;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.utils.NetworkUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.xds.handler.XdsHandler;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * XdsRouterUtils
 *
 * @author zhp
 * @since 2024-12-10
 **/
public class XdsRouterUtils {
    private static final String LOCAL_HOST = "localhost";

    /**
     * the locality information of the host microservice itself
     */
    private static XdsLocality selfServiceLocality;

    private static volatile boolean localityObtainedFlag = false;

    private XdsRouterUtils() {
    }

    /**
     * get XdsLocality of self-service
     *
     * @return XdsLocality
     */
    public static Optional<XdsLocality> getLocalityInfoOfSelfService() {
        if (localityObtainedFlag) {
            return Optional.ofNullable(selfServiceLocality);
        }
        synchronized (XdsRouterUtils.class) {
            if (localityObtainedFlag) {
                return Optional.ofNullable(selfServiceLocality);
            }
            localityObtainedFlag = true;
            String podIp = NetworkUtils.getKubernetesPodIp();
            if (StringUtils.isEmpty(podIp)) {
                return Optional.empty();
            }
            Set<ServiceInstance> serviceInstances = XdsHandler.INSTANCE
                    .getServiceInstanceByServiceName(ConfigManager.getConfig(ServiceMeta.class).getService());
            Optional<ServiceInstance> serviceInstance = getMatchedServiceInstanceByPodIp(serviceInstances, podIp);
            if (!serviceInstance.isPresent()) {
                return Optional.empty();
            }
            Optional<XdsLocality> validXdsLocality = createValidXdsLocality(serviceInstance.get().getMetaData());
            selfServiceLocality = validXdsLocality.orElse(null);
            return validXdsLocality;
        }
    }

    /**
     * update localityObtainedFlag
     *
     * @param flag locality obtained flag
     */
    public static void updateLocalityObtainedFlag(boolean flag) {
        localityObtainedFlag = flag;
    }

    private static Optional<ServiceInstance> getMatchedServiceInstanceByPodIp(Set<ServiceInstance> serviceInstances,
            String podIp) {
        return serviceInstances.stream()
                .filter(serviceInstance -> podIp.equals(serviceInstance.getHost()))
                .findFirst();
    }

    /**
     * rebuild new url by XdsServiceInstance
     *
     * @param oldUri old uri
     * @param serviceInstance xds service instance
     * @return new url
     */
    public static String rebuildUrlByXdsServiceInstance(URI oldUri, ServiceInstance serviceInstance) {
        StringBuilder builder = new StringBuilder();
        builder.append(oldUri.getScheme())
                .append("://")
                .append(serviceInstance.getHost())
                .append(":")
                .append(serviceInstance.getPort())
                .append(oldUri.getPath());
        String query = oldUri.getQuery();
        if (StringUtils.isEmpty(query)) {
            return builder.toString();
        }
        builder.append("?").append(query);
        return builder.toString();
    }

    /**
     * isXdsRouteRequired
     *
     * @param serviceName serviceName
     * @return isXdsRouteRequired
     */
    public static boolean isXdsRouteRequired(String serviceName) {
        // if service is localhost or started not with lowercase, so no xds routing required
        return !StringUtils.isEmpty(serviceName) && !serviceName.equals(LOCAL_HOST)
                && Character.isLowerCase(serviceName.charAt(0));
    }

    private static Optional<XdsLocality> createValidXdsLocality(Map<String, String> metaData) {
        XdsLocality locality = new XdsLocality();
        String region = metaData.get("region");
        String zone = metaData.get("zone");
        String subZone = metaData.get("sub_zone");
        if (StringUtils.isEmpty(region) || StringUtils.isEmpty(zone) && !StringUtils.isEmpty(subZone)) {
            return Optional.empty();
        }
        locality.setRegion(region);
        locality.setZone(zone);
        locality.setSubZone(subZone);
        return Optional.of(locality);
    }
}
