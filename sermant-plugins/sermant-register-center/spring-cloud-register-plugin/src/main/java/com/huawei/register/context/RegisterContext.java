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

package com.huawei.register.context;

import com.huawei.register.handler.SingleStateCloseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 注册中心上下文
 *
 * @author zhouss
 * @since 2021-12-30
 */
public enum RegisterContext {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 注册中心健康监听对象 通常用于关闭定时服务
     */
    private Object registerWatch;

    private final AtomicBoolean isAvailable = new AtomicBoolean(true);

    private final List<SingleStateCloseHandler> closeHandlers = new ArrayList<SingleStateCloseHandler>();

    private Object discoveryClient;

    private final ClientInfo clientInfo = new ClientInfo();

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public boolean compareAndSet(boolean expect, boolean target) {
        return this.isAvailable.compareAndSet(expect, target);
    }

    public boolean isAvailable() {
        return isAvailable.get();
    }

    public Object getRegisterWatch() {
        return registerWatch;
    }

    public void setRegisterWatch(Object registerWatch) {
        this.registerWatch = registerWatch;
    }

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

    public static class ClientInfo {
        /**
         * 服务名 通过拦截获取
         */
        private String serviceName;

        /**
         * 域名
         */
        private String host;

        /**
         * 端口
         */
        private int port;

        /**
         * 服务id
         */
        private String serviceId;

        /**
         * 服务元信息
         */
        private Map<String, String> meta;

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

        public Map<String, String> getMeta() {
            return meta;
        }

        public void setMeta(Map<String, String> meta) {
            this.meta = meta;
        }
    }
}
