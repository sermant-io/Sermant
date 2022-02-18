/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.route.common.gray.constants;

/**
 * 常量
 *
 * @author provenceee
 * @since 2021/10/13
 */
public class GrayConstant {
    /**
     * dubbo参数索引的前缀
     */
    public static final String DUBBO_SOURCE_TYPE_PREFIX = "args";

    /**
     * 地域属性，属于哪个机房
     */
    public static final String GRAY_LDC = "GRAY_LDC";

    /**
     * 上游携带标签
     */
    public static final String GRAY_TAG = "GRAY_TAG";

    /**
     * 灰度发布默认ldc
     */
    public static final String GRAY_DEFAULT_LDC = "DEFAULT_LDC";

    /**
     * 灰度发布默认版本
     */
    public static final String GRAY_DEFAULT_VERSION = "DEFAULT_VERSION";

    /**
     * isEnabled匹配的方法名
     */
    public static final String ENABLED_METHOD_NAME = ".isEnabled()";

    /**
     * 注册时灰度版本的key
     */
    public static final String GRAY_VERSION_KEY = "gray.version";

    /**
     * 注册时ldc的key
     */
    public static final String GRAY_LDC_KEY = "ldc";

    /**
     * DUBBO协议前缀
     */
    public static final String DUBBO_PREFIX = "dubbo://";

    /**
     * 灰度配置servicecomb的key
     */
    public static final String GRAY_CONFIG_SERVICECOMB_KEY = "servicecomb";

    /**
     * 灰度配置routeRule的key
     */
    public static final String GRAY_CONFIG_ROUTE_RULE_KEY = "routeRule";

    /**
     * 灰度配置versionFrom的key
     */
    public static final String GRAY_CONFIG_VERSION_FROM_KEY = "versionFrom";

    /**
     * 注册时版本号
     */
    public static final String REG_VERSION_KEY = "reg.version";

    private GrayConstant() {
    }
}