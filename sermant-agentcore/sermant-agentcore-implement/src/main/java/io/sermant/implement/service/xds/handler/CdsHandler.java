/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on dubbo-xds/src/main/java/org/apache/dubbo/registry/xds/util/protocol/AbstractProtocol.java
 * from the Apache dubbo project.
 */

package io.sermant.implement.service.xds.handler;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import io.sermant.core.service.xds.entity.XdsServiceCluster;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.constants.XdsEnvConstant;
import io.sermant.implement.service.xds.utils.CdsProtocolTransformer;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * CdsHandler
 *
 * @author daizhenyu
 * @since 2024-05-13
 **/
public class CdsHandler extends XdsHandler<Cluster> {
    /**
     * construction method
     *
     * @param client xds client
     */
    public CdsHandler(XdsClient client) {
        super(client);
        this.resourceType = XdsEnvConstant.CDS_RESOURCE_TYPE;
    }

    @Override
    protected void handleResponse(String requestKey, DiscoveryResponse response) {
        Map<String, XdsServiceCluster> oldServiceClusterMap = XdsDataCache.getServiceClusterMap();

        // The contents of the Cds protocol were resolved,
        // and the mapping between the service name and the cluster was updated
        Map<String, XdsServiceCluster> newServiceClusterMap = CdsProtocolTransformer
                .getServiceClusters(decodeResources(response, Cluster.class));
        XdsDataCache.updateServiceClusterMap(newServiceClusterMap);

        // send ack
        StreamObserver<DiscoveryRequest> requestObserver = XdsDataCache.getRequestObserver(requestKey);
        requestObserver.onNext(builtAckDiscoveryRequest(response, Collections.EMPTY_SET));

        updateEdsSubscription(oldServiceClusterMap);
    }

    private void updateEdsSubscription(Map<String, XdsServiceCluster> oldServiceClusterMap) {
        // Eds is updated based on the new service and cluster mapping relationship
        for (Entry<String, StreamObserver<DiscoveryRequest>> entry : XdsDataCache.getRequestObserversEntry()) {
            String key = entry.getKey();
            if (XdsEnvConstant.CDS_ALL_RESOURCE.equals(key)) {
                continue;
            }

            // There is no need to resubscribe when the cluster resources corresponding to the service have not changed.
            Set<String> newServiceClusterResource = XdsDataCache.getClustersByServiceName(key);
            Set<String> oldServiceClusterResource;
            XdsServiceCluster oldServiceCluster = oldServiceClusterMap.get(key);
            if (oldServiceCluster == null) {
                oldServiceClusterResource = Collections.EMPTY_SET;
            } else {
                oldServiceClusterResource = oldServiceCluster.getClusterResources();
            }
            if (newServiceClusterResource
                    .equals(oldServiceClusterResource)) {
                continue;
            }
            StreamObserver<DiscoveryRequest> requestStreamObserver = entry.getValue();
            requestStreamObserver.onNext(buildDiscoveryRequest(XdsEnvConstant.EDS_RESOURCE_TYPE, null, null,
                    XdsDataCache.getClustersByServiceName(key)));
        }
    }

    @Override
    public void subscribe(String requestKey, CountDownLatch countDownLatch) {
        StreamObserver<DiscoveryRequest> requestStreamObserver = client
                .getDiscoveryRequestObserver(getResponseStreamObserver(requestKey, countDownLatch));
        requestStreamObserver.onNext(buildDiscoveryRequest(resourceType, null, null, Collections.EMPTY_SET));
        XdsDataCache.updateRequestObserver(requestKey, requestStreamObserver);
    }

    @Override
    public void subscribe(String requestKey) {
        subscribe(requestKey, null);
    }
}
