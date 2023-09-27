/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.common;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.classloader.FrameworkClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * LoggerFactory
 *
 * @author luanwenfei
 * @version 1.0.0
 * @since 2022-03-26
 */
public class LoggerFactory {
    private static final String LOGGER_FACTORY_IMPL_CLASS = "com.huaweicloud.sermant.implement.log.LoggerFactoryImpl";

    private static final String LOGGER_INIT_METHOD = "init";

    private static Logger defaultLogger;

    private static Logger sermantLogger;

    private LoggerFactory() {
    }

    /**
     * 初始化logback配置文件路径
     *
     * @param artifact 归属产品
     * @throws RuntimeException RuntimeException
     */
    public static void init(String artifact) {
        if (sermantLogger == null) {
            FrameworkClassLoader frameworkClassLoader = ClassLoaderManager.getFrameworkClassLoader();
            try {
                Method initMethod = frameworkClassLoader
                        .loadClass(LOGGER_FACTORY_IMPL_CLASS)
                        .getMethod(LOGGER_INIT_METHOD, String.class);
                sermantLogger = (Logger) initMethod.invoke(null, artifact);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                     | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取jul日志
     *
     * @return jul日志
     */
    public static Logger getLogger() {
        if (sermantLogger != null) {
            return sermantLogger;
        }

        // 避免日志重复获取
        if (defaultLogger == null) {
            defaultLogger = java.util.logging.Logger.getLogger("sermant");
        }
        return defaultLogger;
    }
}