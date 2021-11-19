/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.constants;

/**
 * CSE适配相关参数
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CseConstants {
    /**
     * 服务分隔符
     */
    public static final String SERVICE_SEPARATOR = ",";

    /**
     * 服务版本分隔符
     */
    public static final String SERVICE_VERSION_SEPARATOR = ":";

    /**
     * sc获取服务名的方法名
     */
    public static final String SERVICE_NAME_METHOD = "getName";

    /**
     * sc获取服务版本的方法名
     */
    public static final String SERVICE_VERSION_METHOD = "getVersion";

    /**
     * sc设置project方法
     */
    public static final String PROJECT_METHOD = "setProject";

    /**
     * sc设置自定义标签的方法
     */
    public static final String CUSTOM_LABEL_METHOD = "setCustomLabel";

    /**
     * sc设置自定义标签值的方法
     */
    public static final String CUSTOM_LABEL_VALUE_METHOD = "setCustomLabelValue";

    /**
     * sc设置自定义环境的方法
     */
    public static final String ENVIRONMENT_METHOD = "setEnvironment";

    /**
     * app名
     */
    public static final String APP_NAME_METHOD = "setAppName";
}
