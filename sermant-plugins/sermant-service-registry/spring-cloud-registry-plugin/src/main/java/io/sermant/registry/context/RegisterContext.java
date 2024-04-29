/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.context;

import io.sermant.registry.handler.SingleStateCloseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registry context
 *
 * @author zhouss
 * @since 2021-12-30
 */
public enum RegisterContext {
    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Registry health listeners are typically used to turn off scheduled services
     */
    private Object registerWatch;

    private final AtomicBoolean isAvailable = new AtomicBoolean(true);

    private final List<SingleStateCloseHandler> closeHandlers = new ArrayList<>();

    private Object discoveryClient;

    private final ClientInfo clientInfo = new ClientInfo();

    /**
     * Timed scanner
     */
    private Object scheduleProcessor;

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public Object getScheduleProcessor() {
        return scheduleProcessor;
    }

    public void setScheduleProcessor(Object scheduleProcessor) {
        this.scheduleProcessor = scheduleProcessor;
    }

    /**
     * Set Registry Available Status - Mandatory
     *
     * @param available Availability
     */
    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }

    /**
     * Set the registry availability status
     *
     * @param expect Expectation
     * @param target Target value
     * @return Whether the configuration is successful
     */
    public boolean compareAndSet(boolean expect, boolean target) {
        return this.isAvailable.compareAndSet(expect, target);
    }

    /**
     * Whether the registry is available
     *
     * @return Whether the identity is available or not
     */
    public boolean isAvailable() {
        return isAvailable.get();
    }

    public Object getRegisterWatch() {
        return registerWatch;
    }

    public void setRegisterWatch(Object registerWatch) {
        this.registerWatch = registerWatch;
    }

    /**
     * Register the registry to shut down the processor
     *
     * @param handler Processor
     */
    public void registerCloseHandler(SingleStateCloseHandler handler) {
        if (handler == null) {
            return;
        }
        closeHandlers.add(handler);
    }

    public List<SingleStateCloseHandler> getCloseHandlers() {
        return closeHandlers;
    }

    public Object getDiscoveryClient() {
        return discoveryClient;
    }

    public void setDiscoveryClient(Object discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * Client information
     *
     * @since 2022-03-01
     */
    public static class ClientInfo {
        /**
         * Service name obtained through interception
         */
        private String serviceName;

        /**
         * domain name
         */
        private String host;

        /**
         * ip
         */
        private String ip;

        /**
         * Port
         */
        private int port;

        /**
         * Service ID
         */
        private String serviceId;

        /**
         * Service meta information
         */
        private Map<String, String> meta;

        /**
         * Region
         */
        private String zone;

        /**
         * Instance status UP DOWN
         */
        private String status = "UN_KNOWN";

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public Map<String, String> getMeta() {
            return meta;
        }

        public void setMeta(Map<String, String> meta) {
            this.meta = meta;
        }
    }
}
