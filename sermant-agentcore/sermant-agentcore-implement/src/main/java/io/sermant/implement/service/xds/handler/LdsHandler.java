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
 * Based on dubbo-xds/src/main/java/org/apache/dubbo/registry/xds/util/protocol/impl/LdsProtocol.java
 * from the Apache dubbo project.
 */

package io.sermant.implement.service.xds.handler;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.env.XdsConstant;
import io.sermant.implement.service.xds.utils.LdsProtocolTransformer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * LdsHandler
 *
 * @author daizhenyu
 * @since 2024-07-30
 **/
public class LdsHandler extends XdsHandler<Listener> {
    /**
     * construction method
     *
     * @param client xds client
     */
    public LdsHandler(XdsClient client) {
        super(client);
        this.resourceType = XdsConstant.LDS_RESOURCE_TYPE;
    }

    @Override
    protected void handleResponse(String requestKey, DiscoveryResponse response) {
        Set<String> oldRouteResources = XdsDataCache.getRouteResources();
        XdsDataCache.updateHttpConnectionManagers(LdsProtocolTransformer
                .getHttpConnectionManager(decodeResources(response, Listener.class)));

        // send ack
        StreamObserver<DiscoveryRequest> requestObserver = XdsDataCache.getRequestObserver(requestKey);
        requestObserver.onNext(builtAckDiscoveryRequest(response, Collections.EMPTY_SET));

        updateRdsSubscription(oldRouteResources);
    }

    private void updateRdsSubscription(Set<String> oldRouteResources) {
        // check if the route resources have changed
        Set<String> newRouteResources = XdsDataCache.getRouteResources();
        if (oldRouteResources.equals(newRouteResources)) {
            return;
        }
        StreamObserver<DiscoveryRequest> rdsRequestObserver = XdsDataCache
                .getRequestObserver(XdsConstant.RDS_ALL_RESOURCE);
        rdsRequestObserver.onNext(buildDiscoveryRequest(XdsConstant.RDS_RESOURCE_TYPE, null, null, newRouteResources));
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
