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

package io.sermant.registry.service.client;

import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.utils.ConfigKeyUtil;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Test the basic base class
 *
 * @author zhouss
 * @since 2021-12-25
 */
public class BaseTest {
    protected static Map<String, BaseConfig> configManagerMap;

    protected static ClassLoader currentClassLoader;

    @BeforeClass
    public static void init() throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        currentClassLoader = Thread.currentThread().getContextClassLoader();
        final Class<?> aClass = currentClassLoader.loadClass(PluginConfigManager.class.getName());
        final Field configMap = aClass.getDeclaredField("PLUGIN_CONFIG_MAP");
        configMap.setAccessible(true);
        removeFinalModify(configMap);

        configManagerMap = (Map<String, BaseConfig>) configMap.get(null);
        configManagerMap.put(
                ConfigKeyUtil.getTypeKeyWithClassloader("servicecomb.service", RegisterConfig.class.getClassLoader()),
                new RegisterConfig());
        configManagerMap.put(
                ConfigKeyUtil.getTypeKeyWithClassloader("register.service",
                        RegisterServiceCommonConfig.class.getClassLoader()),
                new RegisterServiceCommonConfig());
    }

    /**
     * Remove the final modifier
     *
     * @param field Field
     * @throws NoSuchFieldException None of these fields is thrown
     * @throws IllegalAccessException Unable to get the field thrown
     */
    protected static void removeFinalModify(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
