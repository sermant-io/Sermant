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

package io.sermant.implement.service.xds.discovery;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsClusterLoadAssigment;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import io.sermant.implement.service.xds.handler.EdsHandler;
import io.sermant.implement.service.xds.utils.XdsCommonUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * xDS service discovery service, Used by the plugin to obtain service instances through the xDS protocol
 *
 * @author daizhenyu
 * @since 2024-05-08
 **/
public class XdsServiceDiscoveryImpl implements XdsServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int TIMEOUT = 5;

    private static final ReentrantLock LOCK = new ReentrantLock();

    private EdsHandler edsHandler;

    /**
     * constructor
     *
     * @param edsHandler eds handler
     */
    public XdsServiceDiscoveryImpl(EdsHandler edsHandler) {
        this.edsHandler = edsHandler;
    }

    /**
     * get service all instance
     *
     * @param serviceName service name
     * @return service instances
     */
    @Override
    public Set<ServiceInstance> getServiceInstance(String serviceName) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        // first check the cache and return if service instance exists
        if (XdsDataCache.isContainsRequestObserver(serviceName)) {
            return XdsDataCache.getServiceInstance(serviceName);
        }

        // locking ensures that a service only creates one stream
        LOCK.lock();
        try {
            // check the cache again after locking and return if service instance exists
            if (XdsDataCache.isContainsRequestObserver(serviceName)) {
                return XdsDataCache.getServiceInstance(serviceName);
            }
            edsHandler.subscribe(serviceName, countDownLatch);
        } finally {
            LOCK.unlock();
        }
        try {
            countDownLatch.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Occur InterruptedException when wait server send message.", e);
        }
        return XdsDataCache.getServiceInstance(serviceName);
    }

    /**
     * get service instance of service cluster
     *
     * @param clusterName cluster name
     * @return XdsClusterInstance
     */
    @Override
    public Optional<XdsClusterLoadAssigment> getClusterServiceInstance(String clusterName) {
        Optional<String> serviceNameOptional = XdsCommonUtils.getServiceNameFromCluster(clusterName);
        if (!serviceNameOptional.isPresent()) {
            return Optional.empty();
        }

        String serviceName = serviceNameOptional.get();

        // first check the cache and return if service instance exists
        if (XdsDataCache.isContainsRequestObserver(serviceName)) {
            return XdsDataCache.getClusterServiceInstance(serviceName, clusterName);
        }

        // locking ensures that a service only creates one stream
        LOCK.lock();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // check the cache again after locking and return if service instance exists
            if (XdsDataCache.isContainsRequestObserver(serviceName)) {
                return XdsDataCache.getClusterServiceInstance(serviceName, clusterName);
            }
            edsHandler.subscribe(serviceName, countDownLatch);
        } finally {
            LOCK.unlock();
        }
        try {
            countDownLatch.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Occur InterruptedException when wait server send message.", e);
        }
        return XdsDataCache.getClusterServiceInstance(serviceName, clusterName);
    }

    /**
     * subscribe service instance by service name, the listener will be triggered when the service instance changes
     *
     * @param serviceName service name
     * @param listener listener
     */
    @Override
    public void subscribeServiceInstance(String serviceName, XdsServiceDiscoveryListener listener) {
        // cache listener
        XdsDataCache.addServiceDiscoveryListener(serviceName, listener);

        // first check the cache and return if service instance exists
        if (XdsDataCache.isContainsRequestObserver(serviceName)) {
            listener.process(XdsDataCache.getServiceInstance(serviceName));
            return;
        }

        // locking ensures that a service only creates one stream
        LOCK.lock();
        try {
            // check the cache again after locking and notify listener if service instance exists
            if (XdsDataCache.isContainsRequestObserver(serviceName)) {
                listener.process(XdsDataCache.getServiceInstance(serviceName));
                return;
            }

            // subscribe service instance
            edsHandler.subscribe(serviceName);
        } finally {
            LOCK.unlock();
        }
    }
}
