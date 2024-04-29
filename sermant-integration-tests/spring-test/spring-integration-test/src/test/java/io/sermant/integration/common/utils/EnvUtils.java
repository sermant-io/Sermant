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

package io.sermant.integration.common.utils;

/**
 * 环境变量工具类
 *
 * @author zhouss
 * @since 2022-08-17
 */
public class EnvUtils {
    private EnvUtils() {
    }

    /**
     * 获取环境变量
     *
     * @param key 键
     * @param defaultValue 默认值
     * @return 环境变量值
     */
    public static String getEnv(String key, String defaultValue) {
        String property = System.getProperty(key);
        if (property == null) {
            property = System.getenv(key);
        }
        if (property == null) {
            return defaultValue;
        }
        return property;
    }
}
