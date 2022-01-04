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

package com.huawei.dubbo.register.config;

import com.huawei.dubbo.register.service.RegistryService;
import com.huawei.sermant.core.service.ServiceManager;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

/**
 * 注册监听器
 *
 * @author provenceee
 * @date 2021/12/15
 */
public class RegistryListener {
    private final RegistryService registryService;

    public RegistryListener() {
        registryService = ServiceManager.getService(RegistryService.class);
    }

    /**
     * ApplicationStartedEvent事件监听器
     */
    @EventListener(value = ApplicationStartedEvent.class)
    public void listen() {
        if (DubboCache.INSTANCE.isLoadSc()) {
            // 加载了sc的注册spi才会注册到sc上面
            registryService.startRegistration();
        }
    }
}