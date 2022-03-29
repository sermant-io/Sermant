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

package com.huawei.sermant.stresstest.config;

import com.huawei.sermant.core.common.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 文件配置类
 *
 * @author yiwei
 * @since 2021-10-25
 */
public class FileConfig extends Config {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String NAME = "stress.properties";

    private static FileConfig config = new FileConfig();

    private Properties properties;

    private String path;

    private FileConfig() {
        properties = loadFile();
    }

    /**
     * 单例模式
     *
     * @return instance
     */
    public static FileConfig getInstance() {
        return config;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 读取 文件
     *
     * @return properties
     */
    private Properties loadFile() {
        File file = new File(path, NAME);
        if (file.isFile()) {
            try {
                return handle(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                // 不会进入
                LOGGER.severe("Cannot find file " + file.getName());
            }
        } else {
            return handle(this.getClass().getClassLoader().getResourceAsStream(NAME));
        }
        return new Properties();
    }

    /**
     * 读取 input stream
     *
     * @param is input stream
     * @return properties
     */
    private Properties handle(InputStream is) {
        Properties localProperties = new Properties();
        try (InputStream local = is) {
            localProperties.load(local);
        } catch (IOException e) {
            LOGGER.severe("Load class file error for reason " + e.getMessage());
        }
        return localProperties;
    }
}
