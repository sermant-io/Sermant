/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.config;

import org.apache.skywalking.apm.agent.core.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 加载mock client配置项
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-25
 * @since 2021-02-25
 */
public class PluginConfig {
    /**
     * mock server url
     */
    public static String mockServerUrl;
    /**
     * http 请求超时时间
     */
    public static int httpTimeout;
    /**
     * status 200 请求成功
     */
    public static int httpSuccessStatus;
    /**
     * mock是否开启
     */
    public static boolean isMock;

    /**
     * 用户自定义的拦截接口
     */
    public static String customEnhanceClass;

    /**
     * 用户自定义的拦截方法
     */
    public static String customEnhanceMethod;

    /**
     * 支持的 mock 类型 dubbo
     */
    public static final String DUBBO = "dubbo";

    /**
     * 支持的 mock 类型 http
     */
    public static final String HTTP = "http";

    /**
     * 支持的 mock 类型 mysql
     */
    public static final String MYSQL = "mysql";

    /**
     * 支持的 mock 类型 redis
     */
    public static final String REDIS = "redis";

    /**
     * 不支持的 mock 类型 返回NoType
     */
    public static final String NOTYPE = "NoType";

    /**
     * 用户自定义的mock类型
     */
    public static final String CUSTOM_TYPE = "customType";

    /**
     * 返回空字符串
     */
    public static final String RETURN_BLANK = "";

    /**
     * dubbo attachments里面的录制任务id字段的key
     */
    public static final String RECORD_JOB_ID = "recordJobId";

    /**
     * dubbo attachements 里面录制时使用的traceId
     */
    public static final String TRACE_ID = "traceId";

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfig.class);

    private PluginConfig() {
    }

    static {
        // 配置用户自定义数据的数量限制
        Config.Correlation.ELEMENT_MAX_NUMBER = 1000;
        try (InputStream inputStream = PluginConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            mockServerUrl = properties.getProperty("mockserver.url");
            httpTimeout = Integer.parseInt(properties.getProperty("http.timeout"));
            httpSuccessStatus = Integer.parseInt(properties.getProperty("http.success.status"));
            isMock = Boolean.valueOf(properties.getProperty("mock.switch"));
            customEnhanceClass = properties.getProperty("custom.enhance.class");
            customEnhanceMethod = properties.getProperty("custom.enhance.method");
        } catch (IOException ioException) {
            isMock = false;
            LOGGER.error("Caught IOException:{} when load InputStream", ioException.getMessage());
        } catch (NumberFormatException numberFormatException) {
            // 获取参数不成功 关闭mock系统
            isMock = false;
            LOGGER.error("Get properties error:{}", numberFormatException.getMessage());
        }
    }
}
