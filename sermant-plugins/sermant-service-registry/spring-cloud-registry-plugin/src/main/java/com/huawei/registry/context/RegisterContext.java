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

package com.huawei.registry.context;

import com.huawei.registry.handler.SingleStateCloseHandler;

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

    private final List<SingleStateCloseHandler> closeHandlers = new ArrayList<>();

    private Object discoveryClient;

    private final ClientInfo clientInfo = new ClientInfo();

    /**
     * 定时扫描器
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
     * 设置注册中心可用状态 - 强制
     *
     * @param available 是否可用
     */
    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }

    /**
     * 设置注册中心可用状态
     *
     * @param expect 期待值
     * @param target 目标值
     * @return 是否配置成功
     */
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

    /**
     * 注册注册中心关闭处理器
     *
     * @param handler 处理器
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
     * 客户端信息
     *
     * @since 2022-03-01
     */
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
         * ip
         */
        private String ip;

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

        /**
         * 区域
         */
        private String zone;

        /**
         * 实例状态 UP DOWN
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
