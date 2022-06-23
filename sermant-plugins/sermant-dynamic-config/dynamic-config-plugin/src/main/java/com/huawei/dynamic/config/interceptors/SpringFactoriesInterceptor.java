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

import com.huawei.dynamic.config.inject.ClassInjectDefine;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 拦截loadFactories注入自定义配置源
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class SpringFactoriesInterceptor extends DynamicConfigSwitchSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final List<ClassInjectDefine> CLASS_DEFINES = new ArrayList<>();

    private final AtomicBoolean isInjected = new AtomicBoolean();

    /**
     * 初始化加载注入定义
     */
    public SpringFactoriesInterceptor() {
        for (ClassInjectDefine define : ServiceLoader.load(ClassInjectDefine.class)) {
            CLASS_DEFINES.add(define);
        }
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        if (isInjected.compareAndSet(false, true)) {
            injectConfigurations(context.getResult());
        }
        return context;
    }

    private void injectConfigurations(Object result) {
        if (result instanceof Map) {
            // spring 高版本处理, 针对List其为不可变list，需做一层处理
            CLASS_DEFINES.forEach(
                classInjectDefine -> injectConfiguration((Map<String, List<String>>) result, classInjectDefine));
        } else {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "[DynamicConfig] Can not inject dynamic configuration! the type of cache is [%s]",
                result.getClass().getName()));
        }
    }

    private void injectConfiguration(Map<String, List<String>> cache, ClassInjectDefine classInjectDefine) {
        final String factoryName = classInjectDefine.factoryName();
        final String className = classInjectDefine.injectClassName();
        if (!classInjectDefine.canInject()) {
            LOGGER.info(String.format(Locale.ENGLISH,
                "[DynamicConfig] class [%s] with factory name [%s] won't be injected due to its precondition",
                className, factoryName));
            return;
        }
        defineInjectClasses(className);
        final ClassInjectDefine[] requiredDefines = classInjectDefine.requiredDefines();
        if (requiredDefines != null && requiredDefines.length > 0) {
            for (ClassInjectDefine define : requiredDefines) {
                injectConfiguration(cache, define);
            }
        }
        if (StringUtils.isBlank(factoryName)) {
            return;
        }
        List<String> configurations = cache.get(factoryName);
        if (configurations != null && !configurations.contains(className)) {
            LOGGER.info(String.format(Locale.ENGLISH, "[DynamicConfig] Injected class [%s] to factory [%s] success!",
                className, factoryName));
            final List<String> newConfigurations = new ArrayList<>(configurations);
            newConfigurations.add(className);
            cache.put(factoryName,
                (cache instanceof MultiValueMap) ? newConfigurations
                    : Collections.unmodifiableList(newConfigurations));
        }
    }

    private void defineInjectClasses(String className) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassUtils.defineClass(className, contextClassLoader);
        LOGGER.info(String.format(Locale.ENGLISH, "[DynamicConfig] Defines class [%s] for classLoader [%s] success!",
            className, contextClassLoader));
    }
}
