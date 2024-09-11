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
 * Based on dubbo-xds/src/main/java/org/apache/dubbo/registry/xds/util/protocol/impl/EdsProtocol.java
 * from the Apache dubbo project.
 */

package io.sermant.implement.service.xds.handler;

import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsServiceClusterLoadAssigment;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.env.XdsConstant;
import io.sermant.implement.service.xds.utils.EdsProtocolTransformer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * EdsHandler
 *
 * @author daizhenyu
 * @since 2024-05-13
 **/
public class EdsHandler extends XdsHandler<ClusterLoadAssignment> {
    /**
     * construction method
     *
     * @param client xds client
     */
    public EdsHandler(XdsClient client) {
        super(client);
        this.resourceType = XdsConstant.EDS_RESOURCE_TYPE;
    }

    @Override
    protected void handleResponse(String requestKey, DiscoveryResponse response) {
        // The contents of the eds protocol were resolved,
        // and get the new instance of service
        XdsServiceClusterLoadAssigment clusterInstance = EdsProtocolTransformer
                .getServiceInstances(decodeResources(response, ClusterLoadAssignment.class));

        // send ack
        StreamObserver<DiscoveryRequest> requestObserver = XdsDataCache.getRequestObserver(requestKey);
        requestObserver.onNext(builtAckDiscoveryRequest(response, XdsDataCache.getClustersByServiceName(requestKey)));

        // check whether the service instance has changed
        Set<ServiceInstance> newInstances = clusterInstance.getServiceInstance();
        Set<ServiceInstance> oldInstances = XdsDataCache.getServiceInstance(requestKey);
        XdsDataCache.updateServiceInstance(requestKey, clusterInstance);
        if (!isInstanceChanged(oldInstances, newInstances)) {
            return;
        }

        // invoke the listener corresponding to service
        List<XdsServiceDiscoveryListener> listeners = XdsDataCache.getServiceDiscoveryListeners(requestKey);
        for (XdsServiceDiscoveryListener listener : listeners) {
            listener.process(newInstances);
        }
    }

    @Override
    public void subscribe(String resourceKey, CountDownLatch countDownLatch) {
        StreamObserver<DiscoveryRequest> requestStreamObserver = client
                .getDiscoveryRequestObserver(getResponseStreamObserver(resourceKey, countDownLatch));
        requestStreamObserver.onNext(buildDiscoveryRequest(resourceType, null, null,
                XdsDataCache.getClustersByServiceName(resourceKey)));
        XdsDataCache.updateRequestObserver(resourceKey, requestStreamObserver);
    }

    @Override
    public void subscribe(String requestKey) {
        subscribe(requestKey, null);
    }

    private boolean isInstanceChanged(Set<ServiceInstance> oldInstances, Set<ServiceInstance> newInstances) {
        if (CollectionUtils.isEmpty(oldInstances) && CollectionUtils.isEmpty(newInstances)) {
            return false;
        }
        if (oldInstances == null || newInstances == null) {
            return true;
        }
        return oldInstances.size() != newInstances.size() || !oldInstances.equals(newInstances);
    }
}