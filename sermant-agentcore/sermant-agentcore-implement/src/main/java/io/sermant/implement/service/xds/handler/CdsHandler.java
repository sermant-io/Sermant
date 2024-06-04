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

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.env.XdsConstant;
import io.sermant.implement.service.xds.utils.XdsProtocolTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

/**
 * CdsHandler
 *
 * @author daizhenyu
 * @since 2024-05-13
 **/
public class CdsHandler extends XdsHandler {
    /**
     * construction method
     *
     * @param client xds client
     */
    public CdsHandler(XdsClient client) {
        super(client);
        this.resourceType = XdsConstant.CDS_RESOURCE_TYPE;
    }

    @Override
    protected void handleResponse(String requestKey, DiscoveryResponse response) {
        Map<String, Set<String>> oldMapping = XdsDataCache.getServiceNameMapping();

        // The contents of the Cds protocol were resolved,
        // and the mapping between the service name and the cluster was updated
        Map<String, Set<String>> newMapping = XdsProtocolTransformer
                .getService2ClusterMapping(decodeResource2Cluster(response));
        XdsDataCache.updateServiceNameMapping(newMapping);

        // send ack
        StreamObserver<DiscoveryRequest> requestObserver = XdsDataCache.getRequestObserver(requestKey);
        requestObserver.onNext(builtAckDiscoveryRequest(response, Collections.EMPTY_SET));

        // Eds is updated based on the new mapping relationship
        for (Entry<String, StreamObserver<DiscoveryRequest>> entry : XdsDataCache.getRequestObserversEntry()) {
            String key = entry.getKey();
            if (XdsConstant.CDS_ALL_RESOURCE.equals(key)) {
                continue;
            }

            // There is no need to resubscribe when the cluster resources corresponding to the service have not changed.
            if (newMapping.getOrDefault(key, Collections.EMPTY_SET)
                    .equals(oldMapping.getOrDefault(key, Collections.EMPTY_SET))) {
                continue;
            }
            StreamObserver<DiscoveryRequest> requestStreamObserver = entry.getValue();
            requestStreamObserver.onNext(buildDiscoveryRequest(XdsConstant.EDS_RESOURCE_TYPE, null, null,
                    XdsDataCache.getClustersByServiceName(key)));
        }
    }

    private List<Cluster> decodeResource2Cluster(DiscoveryResponse response) {
        List<Cluster> clusters = new ArrayList<>();
        for (Any any : response.getResourcesList()) {
            try {
                clusters.add(any.unpack(Cluster.class));
            } catch (InvalidProtocolBufferException e) {
                LOGGER.log(Level.SEVERE, "Decode resource to cluster failed.", e);
            }
        }
        return clusters;
    }

    @Override
    public void subscribe(String requestKey, CountDownLatch countDownLatch) {
        StreamObserver<DiscoveryRequest> requestStreamObserver = client
                .getDiscoveryRequestObserver(getResponseStreamObserver(requestKey, countDownLatch));
        requestStreamObserver.onNext(buildDiscoveryRequest(resourceType, null, null, Collections.EMPTY_SET));
        XdsDataCache.updateRequestObserver(requestKey, requestStreamObserver);
    }
}