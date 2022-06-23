/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.inject;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.ClassUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 注入
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class InjectServiceImpl implements ClassInjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Map<ClassLoader, Set<String>> defineClassCache = new HashMap<>();

    @Override
    public List<String> injectConfiguration(String factoryName, List<String> target,
            ClassInjectDefine classInjectDefine,
            ClassLoader classLoader) {
        final HashMap<String, List<String>> convertMap = new HashMap<>(
                Collections.singletonMap(factoryName, target == null ? new ArrayList<>() : target));
        injectConfiguration(convertMap, classInjectDefine, classLoader, false);
        return convertMap.get(factoryName);
    }

    @Override
    public void injectConfiguration(Map<String, List<String>> target, ClassInjectDefine classInjectDefine,
            ClassLoader classLoader, boolean isUnmodifiable) {
        if (classLoader == null || classInjectDefine == null || target == null) {
            return;
        }
        final String factoryName = classInjectDefine.factoryName();
        final String className = classInjectDefine.injectClassName();
        if (!classInjectDefine.canInject()) {
            LOGGER.fine(String.format(Locale.ENGLISH,
                    "class [%s] with factory name [%s] won't be injected due to its precondition",
                    className, factoryName));
            return;
        }
        defineInjectClasses(className, classLoader);
        final ClassInjectDefine[] requiredDefines = classInjectDefine.requiredDefines();
        if (requiredDefines != null && requiredDefines.length > 0) {
            for (ClassInjectDefine define : requiredDefines) {
                injectConfiguration(target, define, classLoader, isUnmodifiable);
            }
        }
        if (StringUtils.isBlank(factoryName)) {
            return;
        }
        List<String> configurations = target.get(factoryName);
        if (configurations != null && !configurations.contains(className)) {
            LOGGER.fine(String.format(Locale.ENGLISH, "Injected class [%s] to factory [%s] success!",
                    className, factoryName));
            final List<String> newConfigurations = new ArrayList<>(configurations);
            newConfigurations.add(className);
            target.put(factoryName, !isUnmodifiable ? newConfigurations
                    : Collections.unmodifiableList(newConfigurations));
        }
    }

    private void defineInjectClasses(String className, ClassLoader classLoader) {
        final Set<String> injectClasses = defineClassCache.getOrDefault(classLoader, new HashSet<>());
        if (injectClasses.contains(className)) {
            return;
        }
        injectClasses.add(className);
        defineClassCache.put(classLoader, injectClasses);
        ClassUtils.defineClass(className, classLoader);
        LOGGER.fine(String.format(Locale.ENGLISH, "Defines class [%s] for classLoader [%s] success!",
                className, classLoader));
    }

    @Override
    public void stop() {
        defineClassCache.clear();
    }
}
