/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.dubbo.register;

import com.huawei.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * sc注册
 *
 * @author provenceee
 * @date 2021/12/15
 */
public class ServiceCenterRegistry extends FailbackRegistry {
    private static final String PROTOCOL_CONSUMER = "consumer";
    private final List<URL> registers;
    private final RegistryService registryService;

    /**
     * 构造方法
     *
     * @param url url
     */
    public ServiceCenterRegistry(URL url) {
        super(url);
        this.registers = new ArrayList<>();
        this.registryService = ServiceManager.getService(RegistryService.class);
        this.registryService.setServiceCenterRegistry(this);
    }

    @Override
    public void doRegister(URL url) {
        if (!url.getProtocol().equals(PROTOCOL_CONSUMER)) {
            registers.add(url);
        }
    }

    @Override
    public void doUnregister(URL url) {
        registryService.shutdown();
    }

    @Override
    public void doSubscribe(URL url, NotifyListener notifyListener) {
        if (url.getProtocol().equals(PROTOCOL_CONSUMER)) {
            registryService.doSubscribe(new Subscription(url, notifyListener));
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener notifyListener) {
        registryService.shutdown();
    }

    @Override
    public boolean isAvailable() {
        throw new UnsupportedOperationException();
    }

    public List<URL> getRegisters() {
        return registers;
    }
}