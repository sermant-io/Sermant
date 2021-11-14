/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 文件配置类
 *
 * @author yiwei
 * @since 2021/10/25
 */
public class FileConfig extends Config {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String NAME = "stress.properties";

    private static FileConfig config = new FileConfig();

    private Properties properties;

    private String path;

    /**
     * 单例模式
     *
     * @return instance
     */
    public static FileConfig getInstance() {
        return config;
    }

    private FileConfig() {
        properties = loadFile();
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
