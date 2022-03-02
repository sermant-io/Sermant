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

package com.huawei.register.support;

import com.huawei.register.config.RegisterConfig;
import com.huawei.register.entity.MicroServiceInstance;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.utils.ClassLoaderUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
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

    protected final void mark() {
        threadLocal.set(Boolean.TRUE);
    }

    protected final void unMark() {
        threadLocal.remove();
    }

    protected final boolean isMarked() {
        return threadLocal.get() != null;
    }

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
     * @return Object
     */
    protected final Object buildInstance(MicroServiceInstance microServiceInstance) {
        final Class<?> serverClass = getInstanceClass(getInstanceClassName());
        try {
            Constructor<?> declaredConstructor = serverClass
                .getDeclaredConstructor(MicroServiceInstance.class);
            return declaredConstructor.newInstance(microServiceInstance);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
            | InvocationTargetException ignored) {
            return null;
        }
    }

    /**
     * 获取实例类权限定名
     *
     * @return 实例类权限定名
     */
    protected abstract String getInstanceClassName();
}
