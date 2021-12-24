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

package com.huawei.register.service.register;

import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.netflix.loadbalancer.Server;
import org.springframework.cloud.client.ServiceInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

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
     * 注册中心类型 基于spi加载
     * Map<注册中心类型, 注册实现>
     */
    private final Map<String, Register> registerMap = new HashMap<String, Register>();

    /**
     * 当前配置使用的注册实现
     */
    private Register curRegister;

    RegisterManager() {
        loadRegisters();
    }

    private void loadRegisters() {
        // 此处需要使用service的类加载器
        for (Register register : ServiceLoader.load(Register.class, RegisterManager.class.getClassLoader())) {
            final Register.RegisterType registerType = register.registerType();
            if (registerType == null) {
                continue;
            }
            registerMap.put(registerType.name(), register);
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
     * @return Register
     */
    public Register getRegister(Register.RegisterType registerType) {
        return registerMap.get(registerType.name());
    }

    /**
     * 初始化
     */
    public void start() {
        if (isReady()) {
            curRegister.start();
        }
    }

    /**
     * 停止方法
     */
    public void stop() {
        if (isReady()) {
            curRegister.stop();
        }
    }

    /**
     * 注册服务
     *
     * @param rawRegistration 注册信息
     */
    public void register(Object rawRegistration) {
        if (isReady()) {
            curRegister.register(rawRegistration);
        }
    }

    /**
     * 获取服务列表
     *
     * @param <T>    server实现
     * @return 服务列表
     */
    public <T extends Server> List<T> getServerList(String serviceId) {
        if (isReady()) {
            return curRegister.getServerList(serviceId);
        }
        // 若是未将数据准备好的场景，则返回空，不去干扰原注册中心的数据
        return null;
    }

    /**
     * 获取服务实例列表
     *
     * @param serviceId 服务名或者ID
     * @return 服务是咧列表
     */
    public List<ServiceInstance> getInstanceList(String serviceId) {
        return curRegister.getDiscoveryServerList(serviceId);
    }

    private boolean isReady() {
        return (curRegister = getRegister()) != null;
    }

}
