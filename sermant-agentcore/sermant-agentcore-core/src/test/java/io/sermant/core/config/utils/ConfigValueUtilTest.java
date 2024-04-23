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

package io.sermant.core.config.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test environment variable reading
 *
 * @author zhouss
 * @since 2022-07-04
 */
public class ConfigValueUtilTest {
    private static final String CONFIG_DEFAULT_VALUE = "test1";

    private static final String CONFIG_KEY = "service.meta.applicationName";

    private Map<String, String> rawEnvMap;

    /**
     * Tests read environment variables in different formats
     *
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     */
    @Test
    public void testReadEnv() throws Exception {
        testKey(CONFIG_KEY);
        testKey(CONFIG_KEY.replace('.', '_'));
        testKey(CONFIG_KEY.replace('.', '-'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT).replace('.', '-'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT).replace('.', '_'));
        testKey(CONFIG_KEY.toLowerCase(Locale.ROOT));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT).replace('.', '-'));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT).replace('.', '_'));
        testKey(CONFIG_KEY.toUpperCase(Locale.ROOT));
        testKey(transFromCamel(CONFIG_KEY));
        testKey(transFromCamel(CONFIG_KEY).replace('.', '_'));
        testKey(transFromCamel(CONFIG_KEY).replace('.', '-'));
        testKey(transFromCamel(CONFIG_KEY).toUpperCase(Locale.ROOT));
        testKey(transFromCamel(CONFIG_KEY).toUpperCase(Locale.ROOT).replace('.', '_'));
        testKey(transFromCamel(CONFIG_KEY).toUpperCase(Locale.ROOT).replace('.', '-'));
    }

    /**
     * 设置环境变量(win/linux/macos)
     *
     * @param envMap
     * @throws Exception
     */
    private static void setEnv(Map envMap) throws NoSuchFieldException, IllegalAccessException {
        try {
            // win os
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map env = (Map) theEnvironmentField.get(null);
            env.putAll(envMap);

            // linux/macos os
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map cienv = (Map) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(envMap);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map map = (Map) obj;
                    map.clear();
                    map.putAll(envMap);
                }
            }
        }
    }

    private void testKey(String key) throws Exception {
        final Properties properties = System.getProperties();
        properties.put(key, ConfigValueUtilTest.CONFIG_DEFAULT_VALUE);
        final String test = ConfigValueUtil.fixValue(CONFIG_KEY, "test", Collections.emptyMap(), null);
        assertEquals(ConfigValueUtilTest.CONFIG_DEFAULT_VALUE, test);
        properties.remove(key);

        final Map<String, String> envMap = new HashMap<>();
        envMap.put(key, ConfigValueUtilTest.CONFIG_DEFAULT_VALUE);
        setEnv(envMap);
        final String value = ConfigValueUtil.fixValue(CONFIG_KEY, "test", Collections.emptyMap(), null);
        assertEquals(ConfigValueUtilTest.CONFIG_DEFAULT_VALUE, value);
        envMap.remove(key);
    }

    private String transFromCamel(String key) {
        Matcher matcher = Pattern.compile("[A-Z]").matcher(key);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "." + matcher.group(0).toLowerCase(Locale.ROOT));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
