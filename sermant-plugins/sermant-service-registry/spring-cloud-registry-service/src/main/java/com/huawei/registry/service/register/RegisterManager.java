/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.register;

import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.config.RegisterType;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registration Manager
 *
 * @author zhouss
 * @since 2021-12-17
 */
public enum RegisterManager {
    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Registry Type Load Map -> registry type based on SPI, registration implementation
     */
    private final Map<RegisterType, Register> registerMap = new HashMap<>();

    private final AtomicBoolean isRegistered = new AtomicBoolean();

    RegisterManager() {
        loadRegisters();
    }

    private void loadRegisters() {
        // 此处需要使用service的类加载器
        for (Register register : ServiceLoader.load(Register.class, RegisterManager.class.getClassLoader())) {
            final RegisterType registerType = register.registerType();
            if (registerType == null) {
                continue;
            }
            registerMap.put(registerType, register);
        }
    }

    /**
     * Get the registration implementation
     *
     * @return Register
     */
    public Register getRegister() {
        final RegisterServiceCommonConfig pluginConfig =
            PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        return registerMap.get(pluginConfig.getRegisterType());
    }

    /**
     * Get the registration implementation
     *
     * @param registerType Registry type
     * @return Register
     */
    public Register getRegister(RegisterType registerType) {
        return registerMap.get(registerType);
    }

    /**
     * Initialize
     */
    public void start() {
        final Register register = getRegister();
        if (register != null) {
            register.start();
        }
    }

    /**
     * Stop method
     */
    public void stop() {
        final Register register = getRegister();
        if (register != null && isRegistered.get()) {
            register.stop();
        }
    }

    /**
     * Registration Services
     */
    public void register() {
        final Register register = getRegister();
        if (register != null && isRegistered.compareAndSet(false, true)) {
            register.register();
        }
    }

    /**
     * Get a list of services
     *
     * @param serviceId Service ID
     * @param <T>       Instance information
     * @return List of services
     */
    public <T extends MicroServiceInstance> List<T> getServerList(String serviceId) {
        final Register register = getRegister();
        if (register != null) {
            return register.getInstanceList(serviceId);
        }
        return Collections.emptyList();
    }

    /**
     * Get a list of service names
     *
     * @return A list of service names
     */
    public List<String> getServices() {
        final Register register = getRegister();
        if (register != null) {
            return register.getServices();
        }
        return Collections.emptyList();
    }
}
