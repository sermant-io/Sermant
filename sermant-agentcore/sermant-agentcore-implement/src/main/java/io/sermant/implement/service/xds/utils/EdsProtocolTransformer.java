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

package io.sermant.implement.service.xds.utils;

import io.envoyproxy.envoy.config.core.v3.HealthStatus;
import io.envoyproxy.envoy.config.core.v3.Locality;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.service.xds.entity.XdsServiceClusterLoadAssigment;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert eds protocol data to Sermant data model
 *
 * @author daizhenyu
 * @since 2024-05-10
 **/
public class EdsProtocolTransformer {
    private static final int CLUSTER_SPLIT_LENGTH = 4;

    private static final int CLUSTER_SUBSET_INDEX = 2;

    private static final String VERTICAL_LINE_SEPARATOR = "\\|";

    private EdsProtocolTransformer() {
    }

    /**
     * get service instances by xds protocol
     *
     * @param loadAssignments eds data
     * @return instances of service
     */
    public static XdsServiceClusterLoadAssigment getServiceInstances(
            List<ClusterLoadAssignment> loadAssignments) {
        Map<String, XdsClusterLoadAssigment> clusterLoadAssigmentMap = loadAssignments.stream()
                .filter(Objects::nonNull)
                .map(EdsProtocolTransformer::parseClusterLoadAssignment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(XdsClusterLoadAssigment::getClusterName,
                        clusterInstance -> clusterInstance));
        return new XdsServiceClusterLoadAssigment(clusterLoadAssigmentMap,
                getServiceBaseClusterName(clusterLoadAssigmentMap));
    }

    private static Optional<XdsClusterLoadAssigment> parseClusterLoadAssignment(ClusterLoadAssignment loadAssignment) {
        String clusterName = loadAssignment.getClusterName();
        Optional<String> serviceNameOptional = XdsCommonUtils.getServiceNameFromCluster(clusterName);
        if (!serviceNameOptional.isPresent()) {
            return Optional.empty();
        }
        String serviceName = serviceNameOptional.get();
        return Optional.of(new XdsClusterLoadAssigment(serviceName, clusterName,
                parseLocalityLbEndpointsList(loadAssignment, serviceName, clusterName)));
    }

    private static Map<XdsLocality, Set<ServiceInstance>> parseLocalityLbEndpointsList(
            ClusterLoadAssignment loadAssignment,
            String serviceName, String clusterName) {
        List<LocalityLbEndpoints> localityLbEndpointList = loadAssignment.getEndpointsList();
        if (CollectionUtils.isEmpty(localityLbEndpointList)) {
            return Collections.EMPTY_MAP;
        }
        return localityLbEndpointList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        localityLbEndpoints ->
                                parseLocality(localityLbEndpoints),
                        localityLbEndpoints -> parseLocalityLbEndpoints(localityLbEndpoints, serviceName, clusterName)
                ));
    }

    private static Set<ServiceInstance> parseLocalityLbEndpoints(LocalityLbEndpoints localityLbEndpoints,
            String serviceName, String clusterName) {
        List<LbEndpoint> lbEndpointsList = localityLbEndpoints.getLbEndpointsList();
        if (CollectionUtils.isEmpty(lbEndpointsList)) {
            return Collections.EMPTY_SET;
        }
        return lbEndpointsList.stream()
                .filter(Objects::nonNull)
                .map(lbEndpoint -> parseLbEndpoint(lbEndpoint, serviceName, clusterName,
                        getInitializedMetadata(localityLbEndpoints)))
                .collect(Collectors.toSet());
    }

    private static Map<String, String> getInitializedMetadata(LocalityLbEndpoints localityLbEndpoints) {
        Map<String, String> metadata = new HashMap<>();
        Locality locality = localityLbEndpoints.getLocality();
        if (locality != null) {
            metadata.put("region", locality.getRegion());
            metadata.put("zone", locality.getZone());
            metadata.put("sub_zone", locality.getSubZone());
        }
        return metadata;
    }

    private static ServiceInstance parseLbEndpoint(LbEndpoint endpoint, String serviceName,
            String clusterName, Map<String, String> metadata) {
        XdsServiceInstance instance = new XdsServiceInstance();
        SocketAddress socketAddress = endpoint.getEndpoint().getAddress().getSocketAddress();
        instance.setService(serviceName);
        instance.setCluster(clusterName);
        instance.setHost(socketAddress.getAddress());
        instance.setPort(socketAddress.getPortValue());
        endpoint.getMetadata().getFilterMetadataMap().values()
                .forEach(struct -> struct.getFieldsMap()
                        .forEach((key, value) -> metadata.put(key, value.getStringValue())));
        instance.setMetadata(metadata);
        if (HealthStatus.HEALTHY.equals(endpoint.getHealthStatus())
                || HealthStatus.UNKNOWN.equals(endpoint.getHealthStatus())) {
            instance.setHealthStatus(true);
            return instance;
        }
        instance.setHealthStatus(false);
        return instance;
    }

    private static String getServiceBaseClusterName(Map<String, XdsClusterLoadAssigment> instanceMap) {
        for (Entry<String, XdsClusterLoadAssigment> instanceEntry : instanceMap.entrySet()) {
            String cluster = instanceEntry.getKey();
            String[] splitCluster = cluster.split(VERTICAL_LINE_SEPARATOR);
            if (splitCluster.length == CLUSTER_SPLIT_LENGTH && splitCluster[CLUSTER_SUBSET_INDEX].equals("")) {
                return cluster;
            }
        }
        return "";
    }

    private static XdsLocality parseLocality(LocalityLbEndpoints localityLbEndpoints) {
        Locality locality = localityLbEndpoints.getLocality();
        return new XdsLocality(locality.getRegion(), locality.getZone(), locality.getSubZone(),
                localityLbEndpoints.getLoadBalancingWeight().getValue(), localityLbEndpoints.getPriority());
    }
}
