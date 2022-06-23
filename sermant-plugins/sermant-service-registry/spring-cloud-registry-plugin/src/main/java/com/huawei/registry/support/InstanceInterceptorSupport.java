/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.support;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ClassLoaderUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例获取拦截器支持
 *
 * @author zhouss
 * @since 2022-02-22
 */
public abstract class InstanceInterceptorSupport extends RegisterSwitchSupport {
    private final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    /**
     * 类缓存, 避免多次调用loadClass
     */
    private final Map<String, Class<?>> cacheClasses = new ConcurrentHashMap<>();

    private RegisterConfig config;

    /**
     * 标记当前线程方法调用
     * <p></p>
     * 默认标记, 确保下游调用不会存在再次mark的场景
     */
    protected final void mark() {
        threadLocal.set(Boolean.TRUE);
    }

    /**
     * 默认去除标记
     */
    protected final void unMark() {
        threadLocal.remove();
    }

    /**
     * 判断是否被标记
     *
     * @return 是否被标记
     */
    protected final boolean isMarked() {
        return threadLocal.get() != null;
    }

    /**
     * 是否开启注册中心迁移，双注册
     *
     * @return 是否开启
     */
    protected final boolean isOpenMigration() {
        return getRegisterConfig().isOpenMigration();
    }

    private RegisterConfig getRegisterConfig() {
        if (config == null) {
            config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        }
        return config;
    }

    /**
     * 获取实例类对象 当不存在时，采用宿主类加载器加载，使之可与宿主关联
     *
     * @param className 宿主全限定类名
     * @return 宿主类
     */
    protected final Class<?> getInstanceClass(String className) {
        return cacheClasses.computeIfAbsent(className, fn -> {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> result = null;
            try {
                result = ClassLoaderUtils.defineClass(className, contextClassLoader,
                    ClassLoaderUtils.getClassResource(ClassLoader.getSystemClassLoader(), className));
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | IOException e) {
                // 有可能已经加载过了，直接用contextClassLoader.loadClass加载
                try {
                    result = contextClassLoader.loadClass(className);
                } catch (ClassNotFoundException ignored) {
                    // ignored
                }
            }
            return result;
        });
    }

    /**
     * 构建实例  由子类自行转换
     *
     * @param microServiceInstance 实例信息
     * @param serviceName          服务名
     * @return Object
     */
    protected final Optional<Object> buildInstance(MicroServiceInstance microServiceInstance, String serviceName) {
        final Class<?> serverClass = getInstanceClass(getInstanceClassName());
        try {
            Constructor<?> declaredConstructor = serverClass
                .getDeclaredConstructor(MicroServiceInstance.class, String.class);
            return Optional.of(declaredConstructor.newInstance(microServiceInstance, serviceName));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 宿主是否为webflux应用
     *
     * @param target 增强目标
     * @return 是返回true
     */
    protected boolean isWebfLux(Object target) {
        return StringUtils.equals("org.springframework.cloud.client.discovery.composite.reactive"
                + ".ReactiveCompositeDiscoveryClient", target.getClass().getName());
    }

    /**
     * 获取实例类权限定名
     *
     * @return 实例类权限定名
     */
    protected abstract String getInstanceClassName();
}
