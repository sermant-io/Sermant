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

package com.huawei.flowcontrol.common.adapte.cse.constants;

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
     * project 缺省值
     */
    public static final String DEFAULT_PROJECT = "default";

    /**
     * 自定义标签缺省值
     */
    public static final String DEFAULT_CUSTOM_LABEL = "public";

    /**
     * 自定义标签缺省值
     */
    public static final String DEFAULT_CUSTOM_LABEL_VALUE = "";

    /**
     * dubbo默认服务名称
     */
    public static final String DEFAULT_DUBBO_SERVICE_NAME = "defaultMicroserviceName";

    /**
     * dubbo版本缺省值
     */
    public static final String DEFAULT_DUBBO_VERSION = "";

    /**
     * Dubbo默认环境
     */
    public static final String DEFAULT_DUBBO_ENVIRONMENT = "";

    /**
     * dubbo默认appname
     */
    public static final String DEFAULT_DUBBO_APP_NAME = "default";

    /**
     * =================================dubbo========================== PROJECT
     */
    public static final String KEY_DUBBO_KIE_PROJECT = "dubbo.servicecomb.service.project";

    /**
     * 服务名键
     */
    public static final String KEY_DUBBO_SERVICE_NAME = "dubbo.servicecomb.service.name";

    /**
     * app名 键
     */
    public static final String KEY_DUBBO_APP_NAME = "dubbo.servicecomb.service.application";

    /**
     * 环境 键
     */
    public static final String KEY_DUBBO_ENVIRONMENT = "dubbo.servicecomb.service.environment";

    /**
     * 自定义标签 键
     */
    public static final String KEY_DUBBO_CUSTOM_LABEL = "dubbo.servicecomb.config.customLabel";

    /**
     * 自定义标签 值
     */
    public static final String KEY_DUBBO_CUSTOM_LABEL_VALUE = "dubbo.servicecomb.config.customLabelValue";

    /**
     * 版本
     */
    public static final String KEY_DUBBO_VERSION = "dubbo.servicecomb.service.version";

    /**
     * ================================spring boot============================== PROJECT
     */
    public static final String KEY_SPRING_KIE_PROJECT = "spring.cloud.servicecomb.credentials.project";

    /**
     * 服务名键
     */
    public static final String KEY_SPRING_SERVICE_NAME = "spring.cloud.servicecomb.discovery.serviceName";

    /**
     * app名 键
     */
    public static final String KEY_SPRING_APP_NAME = "spring.cloud.servicecomb.discovery.appName";

    /**
     * 环境 键
     */
    public static final String KEY_SPRING_ENVIRONMENT = "server.env";

    /**
     * 自定义标签 键
     */
    public static final String KEY_SPRING_CUSTOM_LABEL = "spring.cloud.servicecomb.config.kie.customLabel";

    /**
     * 自定义标签 值
     */
    public static final String KEY_SPRING_CUSTOM_LABEL_VALUE = "spring.cloud.servicecomb.config.kie.customLabelValue";

    /**
     * 版本
     */
    public static final String KEY_SPRING_VERSION = "spring.cloud.servicecomb.discovery.version";

    private CseConstants() {
    }
}
