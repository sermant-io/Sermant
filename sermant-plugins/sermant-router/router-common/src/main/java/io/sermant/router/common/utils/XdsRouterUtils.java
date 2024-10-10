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

package io.sermant.router.common.utils;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.utils.NetworkUtils;
import io.sermant.core.utils.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * XdsRouterUtils
 *
 * @author daizhenyu
 * @since 2024-08-29
 **/
public class XdsRouterUtils {
    private static XdsServiceDiscovery serviceDiscovery = ServiceManager.getService(XdsCoreService.class)
            .getXdsServiceDiscovery();

    /**
     * the locality information of the host microservice itself
     */
    private static XdsLocality selfServiceLocality;

    private XdsRouterUtils() {
    }

    /**
     * get XdsLocality of self-service
     *
     * @return XdsLocality
     */
    public static Optional<XdsLocality> getLocalityInfoOfSelfService() {
        if (selfServiceLocality != null) {
            return Optional.of(selfServiceLocality);
        }
        synchronized (XdsRouterUtils.class) {
            if (selfServiceLocality != null) {
                return Optional.of(selfServiceLocality);
            }
            String podIp = NetworkUtils.getKubernetesPodIp();
            if (StringUtils.isEmpty(podIp)) {
                return Optional.empty();
            }
            Set<ServiceInstance> serviceInstances = serviceDiscovery
                    .getServiceInstance(ConfigManager.getConfig(ServiceMeta.class).getService());
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
     * updateServiceDiscovery
     *
     * @param xdsServiceDiscovery xdsServiceDiscovery
     */
    public static void updateServiceDiscovery(XdsServiceDiscovery xdsServiceDiscovery) {
        if (xdsServiceDiscovery != null) {
            serviceDiscovery = xdsServiceDiscovery;
        }
    }

    private static Optional<ServiceInstance> getMatchedServiceInstanceByPodIp(Set<ServiceInstance> serviceInstances,
            String podIp) {
        return serviceInstances.stream()
                .filter(serviceInstance -> podIp.equals(serviceInstance.getHost()))
                .findFirst();
    }

    private static Optional<XdsLocality> createValidXdsLocality(Map<String, String> metaData) {
        XdsLocality locality = new XdsLocality();
        String region = metaData.get("region");
        String zone = metaData.get("zone");
        String subZone = metaData.get("sub_zone");
        if (StringUtils.isEmpty(region) || (StringUtils.isEmpty(zone) && !StringUtils.isEmpty(subZone))) {
            return Optional.empty();
        }
        locality.setRegion(region);
        locality.setZone(zone);
        locality.setSubZone(subZone);
        return Optional.of(locality);
    }
}
