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

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.core.v3.HealthStatus;
import io.envoyproxy.envoy.config.core.v3.Locality;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LocalityLbEndpoints;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.implement.service.xds.entity.XdsServiceInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert xDS protocol data to Sermant data model
 *
 * @author daizhenyu
 * @since 2024-05-10
 **/
public class XdsProtocolTransformer {
    private static final int SERVICE_HOST_INDEX = 3;

    private static final int SERVICE_NAME_INDEX = 0;

    private static final int EXPECT_LENGTH = 4;

    private XdsProtocolTransformer() {
    }

    /**
     * get the mapping between service name of k8s and cluster of istio
     *
     * @param clusters clusters
     * @return mapping
     */
    public static Map<String, Set<String>> getService2ClusterMapping(List<Cluster> clusters) {
        Map<String, Set<String>> nameMapping = new HashMap<>();
        for (Cluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            Optional<String> serviceNameFromCluster = getServiceNameFromCluster(cluster.getName());
            if (!serviceNameFromCluster.isPresent()) {
                continue;
            }
            String serviceName = serviceNameFromCluster.get();
            nameMapping.computeIfAbsent(serviceName, key -> new HashSet<>()).add(cluster.getName());
        }
        return nameMapping;
    }

    /**
     * get the instance of one service by xds protocol
     *
     * @param loadAssignments eds data
     * @return instance of service
     */
    public static Set<ServiceInstance> getServiceInstances(
            List<ClusterLoadAssignment> loadAssignments) {
        return loadAssignments.stream()
                .filter(Objects::nonNull)
                .flatMap(loadAssignment -> getServiceInstancesFromLoadAssignment(loadAssignment).stream())
                .collect(Collectors.toSet());
    }

    private static Set<ServiceInstance> getServiceInstancesFromLoadAssignment(ClusterLoadAssignment loadAssignment) {
        String clusterName = loadAssignment.getClusterName();
        Optional<String> serviceNameOptional = getServiceNameFromCluster(clusterName);
        if (!serviceNameOptional.isPresent()) {
            return Collections.EMPTY_SET;
        }
        String serviceName = serviceNameOptional.get();
        return processClusterLoadAssignment(loadAssignment, serviceName, clusterName);
    }

    private static Set<ServiceInstance> processClusterLoadAssignment(ClusterLoadAssignment loadAssignment,
            String serviceName, String clusterName) {
        List<LocalityLbEndpoints> localityLbEndpointList = loadAssignment.getEndpointsList();
        if (CollectionUtils.isEmpty(localityLbEndpointList)) {
            return Collections.EMPTY_SET;
        }
        return localityLbEndpointList.stream()
                .filter(Objects::nonNull)
                .flatMap(localityLbEndpoints -> processLocalityLbEndpoints(localityLbEndpoints, serviceName,
                        clusterName).stream())
                .collect(Collectors.toSet());
    }

    private static Set<ServiceInstance> processLocalityLbEndpoints(LocalityLbEndpoints localityLbEndpoints,
            String serviceName, String clusterName) {
        List<LbEndpoint> lbEndpointsList = localityLbEndpoints.getLbEndpointsList();
        if (CollectionUtils.isEmpty(lbEndpointsList)) {
            return Collections.EMPTY_SET;
        }
        return lbEndpointsList.stream()
                .filter(Objects::nonNull)
                .map(lbEndpoint -> transformEndpoint2Instance(lbEndpoint, serviceName, clusterName,
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

    private static ServiceInstance transformEndpoint2Instance(LbEndpoint endpoint, String serviceName,
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

    private static Optional<String> getServiceNameFromCluster(String clusterName) {
        if (StringUtils.isEmpty(clusterName)) {
            return Optional.empty();
        }

        // cluster name format: "outbound|8080||xds-service.default.svc.cluster.local", xds-service is service name
        String[] clusterSplit = clusterName.split("\\|");
        if (clusterSplit.length != EXPECT_LENGTH) {
            return Optional.empty();
        }
        return Optional.of(clusterSplit[SERVICE_HOST_INDEX].split("\\.")[SERVICE_NAME_INDEX]);
    }
}
