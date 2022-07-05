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

package com.huaweicloud.sermant.core.config.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 测试环境变量读取
 *
 * @author zhouss
 * @since 2022-07-04
 */
public class ConfigValueUtilTest {
    private static final String CONFIG_DEFAULT_VALUE = "test1";
    private static final String CONFIG_KEY = "service.meta.applicationName";

    private Map<String, String> rawEnvMap;

    /**
     * 测试读取不同格式的环境变量
     *
     * @throws NoSuchFieldException 不会抛出
     * @throws IllegalArgumentException 不会抛出
     * @throws ClassNotFoundException 不会抛出
     */
    @Test
    public void testReadEnv() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        testKey(CONFIG_KEY);
        testKey(CONFIG_KEY.replace('.', '_'));
        testKey(CONFIG_KEY.replace('.', '-'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT).replace('.', '-'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT).replace('.', '_'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT).replace('.', '-'));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT).replace('.', '_'));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT));
    }

    private Map<String, String> getEnvMap()
            throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        if (rawEnvMap == null) {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            final Class<?> aClass = contextClassLoader.loadClass("java.lang.ProcessEnvironment");
            final Field theEnvironment = aClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theEnvironment.setAccessible(true);
            rawEnvMap = (Map<String, String>) theEnvironment.get(null);
        }
        return rawEnvMap;
    }

    private void testKey(String key) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        final Properties properties = System.getProperties();
        properties.put(key, ConfigValueUtilTest.CONFIG_DEFAULT_VALUE);
        final String test = ConfigValueUtil.fixValue(key, "test", Collections.emptyMap(), null);
        assertEquals(ConfigValueUtilTest.CONFIG_DEFAULT_VALUE, test);
        properties.remove(key);

        final Map<String, String> envMap = getEnvMap();
        envMap.put(key, ConfigValueUtilTest.CONFIG_DEFAULT_VALUE);
        final String value = ConfigValueUtil.fixValue(key, "test", Collections.emptyMap(), null);
        assertEquals(ConfigValueUtilTest.CONFIG_DEFAULT_VALUE, value);
        envMap.remove(key);
    }
}
