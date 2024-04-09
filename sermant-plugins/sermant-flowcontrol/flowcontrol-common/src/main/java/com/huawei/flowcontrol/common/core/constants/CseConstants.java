/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.core.constants;

/**
 * cse adaptation parameters
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class CseConstants {
    /**
     * number of blocks cut by service version
     */
    public static final int SERVICE_VERSION_PARTS = 2;

    /**
     * service separator
     */
    public static final String SERVICE_SEPARATOR = ",";

    /**
     * service version separator
     */
    public static final String SERVICE_VERSION_SEPARATOR = ":";

    /**
     * project default value
     */
    public static final String DEFAULT_PROJECT = "default";

    /**
     * user defined label default values
     */
    public static final String DEFAULT_CUSTOM_LABEL = "public";

    /**
     * user defined label default values
     */
    public static final String DEFAULT_CUSTOM_LABEL_VALUE = "";

    /**
     * dubbo default service name
     */
    public static final String DEFAULT_DUBBO_SERVICE_NAME = "defaultMicroserviceName";

    /**
     * default value of dubbo version
     */
    public static final String DEFAULT_DUBBO_VERSION = "";

    /**
     * default dubbo environment
     */
    public static final String DEFAULT_DUBBO_ENVIRONMENT = "";

    /**
     * dubbo default appname
     */
    public static final String DEFAULT_DUBBO_APP_NAME = "default";

    /**
     * =================================dubbo========================== PROJECT
     */
    public static final String KEY_DUBBO_KIE_PROJECT = "dubbo.servicecomb.service.project";

    /**
     * service name key
     */
    public static final String KEY_DUBBO_SERVICE_NAME = "dubbo.servicecomb.service.name";

    /**
     * app name key
     */
    public static final String KEY_DUBBO_APP_NAME = "dubbo.servicecomb.service.application";

    /**
     * environment key
     */
    public static final String KEY_DUBBO_ENVIRONMENT = "dubbo.servicecomb.service.environment";

    /**
     * customize the label key
     */
    public static final String KEY_DUBBO_CUSTOM_LABEL = "dubbo.servicecomb.config.customLabel";

    /**
     * custom label values
     */
    public static final String KEY_DUBBO_CUSTOM_LABEL_VALUE = "dubbo.servicecomb.config.customLabelValue";

    /**
     * version
     */
    public static final String KEY_DUBBO_VERSION = "dubbo.servicecomb.service.version";

    /**
     * ================================spring boot============================== PROJECT
     */
    public static final String KEY_SPRING_KIE_PROJECT = "spring.cloud.servicecomb.credentials.project";

    /**
     * service name key
     */
    public static final String KEY_SPRING_SERVICE_NAME = "spring.cloud.servicecomb.discovery.serviceName";

    /**
     * app name key
     */
    public static final String KEY_SPRING_APP_NAME = "spring.cloud.servicecomb.discovery.appName";

    /**
     * environment key
     */
    public static final String KEY_SPRING_ENVIRONMENT = "server.env";

    /**
     * customize the label key
     */
    public static final String KEY_SPRING_CUSTOM_LABEL = "spring.cloud.servicecomb.config.kie.customLabel";

    /**
     * custom label values
     */
    public static final String KEY_SPRING_CUSTOM_LABEL_VALUE = "spring.cloud.servicecomb.config.kie.customLabelValue";

    /**
     * version
     */
    public static final String KEY_SPRING_VERSION = "spring.cloud.servicecomb.discovery.version";

    private CseConstants() {
    }
}
