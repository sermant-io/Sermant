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
import com.netflix.client.config.IClientConfig;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;

import java.util.ArrayList;
import java.util.List;
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
     * 注册中心健康监听对象
     * 通常用于关闭定时服务
     */
    private Object registerWatch;

    /**
     * 当前实例注册基本信息
     */
    private IClientConfig iClientConfig;

    private final AtomicBoolean isAvailable = new AtomicBoolean(true);

    private final List<SingleStateCloseHandler> closeHandlers = new ArrayList<SingleStateCloseHandler>();

    private CompositeDiscoveryClient discoveryClient;

    public void setAvailable(boolean isAvailable) {
        this.isAvailable.set(isAvailable);
    }

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

    public CompositeDiscoveryClient getDiscoveryClient() {
        return discoveryClient;
    }

    public void setDiscoveryClient(CompositeDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public IClientConfig getiClientConfig() {
        return iClientConfig;
    }

    public void setiClientConfig(IClientConfig iClientConfig) {
        this.iClientConfig = iClientConfig;
    }
}
