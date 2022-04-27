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

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.utils.ClassUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 拦截loadFactories注入自定义配置源
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class SpringFactoriesInterceptor extends DynamicConfigSwitchSupport {
    private static final String PROPERTY_LOCATOR_CLASS = "com.huawei.dynamic.config.source.SpringPropertyLocator";

    private static final String PROPERTY_SOURCE_CLASS = "com.huawei.dynamic.config.source.SpringPropertySource";

    private static final String EVENT_PUBLISHER_CLASS = "com.huawei.dynamic.config.source.SpringEventPublisher";

    private static final String BOOTSTRAP_FACTORY_NAME = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    private static final String ENABLE_AUTO_CONFIGURATION_FACTORY_NAME =
        "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    private final AtomicBoolean isInjected = new AtomicBoolean();

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        if (isInjected.compareAndSet(false, true)) {
            defineInjectClasses();
            injectConfigurations(context.getResult());
        }
        return context;
    }

    private void injectConfigurations(Object result) {
        if (result.getClass().isAssignableFrom(MultiValueMap.class)) {
            injectWithOldVersion((MultiValueMap<String, String>) result);
        } else if (result instanceof Map) {
            // spring 高版本处理, 针对List其为不可变list，需做一层处理
            injectWithNewVersion((Map<String, List<String>>) result);
        } else {
            LoggerFactory.getLogger().warning(String.format(Locale.ENGLISH,
                "Can not inject dynamic configuration! the type of cache is [%s]", result.getClass().getName()));
        }
    }

    /**
     * 针对Spring旧版本注入
     *
     * @param cache 各类Factory权限定名缓存
     */
    private void injectWithOldVersion(MultiValueMap<String, String> cache) {
        List<String> bootstrapConfigurations = cache.get(BOOTSTRAP_FACTORY_NAME);
        if (!bootstrapConfigurations.contains(PROPERTY_LOCATOR_CLASS)) {
            bootstrapConfigurations.add(PROPERTY_LOCATOR_CLASS);
        }
        final List<String> autoConfigurations = cache.get(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME);
        if (!autoConfigurations.contains(EVENT_PUBLISHER_CLASS)) {
            autoConfigurations.add(EVENT_PUBLISHER_CLASS);
        }
    }

    /**
     * 针对Spring新版本注入
     *
     * @param cache 各类Factory权限定名缓存
     */
    private void injectWithNewVersion(Map<String, List<String>> cache) {
        injectConfiguration(cache, BOOTSTRAP_FACTORY_NAME, PROPERTY_LOCATOR_CLASS);
        injectConfiguration(cache, ENABLE_AUTO_CONFIGURATION_FACTORY_NAME, EVENT_PUBLISHER_CLASS);
    }

    private void injectConfiguration(Map<String, List<String>> cache, String factoryName, String className) {
        List<String> bootstrapConfigurations = cache.get(factoryName);
        if (!bootstrapConfigurations.contains(className)) {
            final List<String> newBootstrapConfigurations = new ArrayList<>(bootstrapConfigurations);
            newBootstrapConfigurations.add(className);
            cache.put(factoryName, Collections.unmodifiableList(newBootstrapConfigurations));
        }
    }

    /**
     * 只能调用一次, 定义注入类, 必须排在injectConfigurations之前调用
     */
    private void defineInjectClasses() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassUtils.defineClass(PROPERTY_SOURCE_CLASS, contextClassLoader);
        ClassUtils.defineClass(PROPERTY_LOCATOR_CLASS, contextClassLoader);
        ClassUtils.defineClass(EVENT_PUBLISHER_CLASS, contextClassLoader);
    }
}
