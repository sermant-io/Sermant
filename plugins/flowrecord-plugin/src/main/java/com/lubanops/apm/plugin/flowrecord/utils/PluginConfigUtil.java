/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.utils;

import com.lubanops.apm.plugin.flowrecord.config.ConfigConst;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置信息工具类
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 */
public class PluginConfigUtil {
    private static final ILog LOGGER = LogManager.getLogger(PluginConfigUtil.class);
    private static Properties properties;
    private static String configFileName = null;
    private static String active;

    static {
        active = ConfigConst.CONFIG_PROFILE_ACTIVE;

        configFileName = "/config-" + active + ".properties";
        InputStream in = null;
        try {
            in = PluginConfigUtil.class.getResourceAsStream(configFileName);
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            LOGGER.info("[flowrecord]: cannot get properties");
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.info("[flowrecord]: cannot close the file");
                }
            }
        }
    }

    public static String getValueByKey(String key) {
        return String.valueOf(properties.getOrDefault(key, "")).trim();
    }
}
