/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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
    public static final String SERVICE_NAME_METHOD = "setServiceName";

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
