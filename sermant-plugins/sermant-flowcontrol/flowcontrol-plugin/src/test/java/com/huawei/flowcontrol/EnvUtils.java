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

package com.huawei.flowcontrol;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * 环境变量工具类
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class EnvUtils {
    private static void handEnv(Consumer<Map<String, String>> envHandler)
            throws NoSuchFieldException, IllegalAccessException {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            envHandler.accept(env);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> caseEnv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            envHandler.accept(caseEnv);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear ();
                    envHandler.accept(map);
                }
            }
        }
    }

    /**
     * 环境变量工具类
     *
     * @param newEnv 新的环境变量
     * @throws Exception 设置失败报错
     */
    public static void addEnv(Map<String, String> newEnv) throws Exception {
        final Properties properties = System.getProperties();
        properties.putAll(newEnv);
        handEnv(env -> {
            env.putAll(newEnv);
        });
    }

    /**
     * 删除环境变量
     *
     * @param newEnv 新的环境变量
     * @throws Exception 设置失败报错
     */
    public static void delEnv(Map<String, String> newEnv) throws Exception {
        final Properties properties = System.getProperties();
        newEnv.forEach((k, v) -> properties.remove(k));
        handEnv(env -> {
            newEnv.forEach((k, v) -> env.remove(k));
        });
    }
}
