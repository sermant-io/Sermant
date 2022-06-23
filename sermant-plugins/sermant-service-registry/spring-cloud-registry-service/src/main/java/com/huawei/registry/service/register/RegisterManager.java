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

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
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
 * 注册管理器
 *
 * @author zhouss
 * @since 2021-12-17
 */
public enum RegisterManager {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 注册中心类型 基于spi加载 Map -> 注册中心类型, 注册实现
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
     * 获取注册实现
     *
     * @return Register
     */
    public Register getRegister() {
        final RegisterConfig pluginConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        return registerMap.get(pluginConfig.getRegisterType());
    }

    /**
     * 获取注册实现
     *
     * @param registerType 注册中心类型
     * @return Register
     */
    public Register getRegister(RegisterType registerType) {
        return registerMap.get(registerType);
    }

    /**
     * 初始化
     */
    public void start() {
        final Register register = getRegister();
        if (register != null) {
            register.start();
        }
    }

    /**
     * 停止方法
     */
    public void stop() {
        final Register register = getRegister();
        if (register != null && isRegistered.get()) {
            register.stop();
        }
    }

    /**
     * 注册服务
     */
    public void register() {
        final Register register = getRegister();
        if (register != null && isRegistered.compareAndSet(false, true)) {
            register.register();
        }
    }

    /**
     * 获取启动延迟时间
     *
     * @return 延迟时间, 单位秒
     */
    private long getStartDelayTime() {
        final GraceConfig pluginConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        if (pluginConfig.isEnableSpring()) {
            return pluginConfig.getStartDelayTime();
        }
        return 0L;
    }

    /**
     * 获取服务列表
     *
     * @param serviceId 服务ID
     * @param <T>       实例信息
     * @return 服务列表
     */
    public <T extends MicroServiceInstance> List<T> getServerList(String serviceId) {
        final Register register = getRegister();
        if (register != null) {
            return register.getInstanceList(serviceId);
        }
        return Collections.emptyList();
    }

    /**
     * 获取服务名列表
     *
     * @return 服务名列表
     */
    public List<String> getServices() {
        final Register register = getRegister();
        if (register != null) {
            return register.getServices();
        }
        return Collections.emptyList();
    }
}
