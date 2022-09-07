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

package com.huaweicloud.sermant.router.common.constants;

/**
 * 常量
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class RouterConstant {
    /**
     * dubbo参数索引的前缀
     */
    public static final String DUBBO_SOURCE_TYPE_PREFIX = "args";

    /**
     * 标签路由默认版本
     */
    public static final String ROUTER_DEFAULT_VERSION = "0.0.0";

    /**
     * isEnabled匹配的方法名
     */
    public static final String ENABLED_METHOD_NAME = ".isEnabled()";

    /**
     * 标签路由servicecomb的key
     */
    public static final String ROUTER_CONFIG_SERVICECOMB_KEY = "servicecomb";

    /**
     * 标签路由routeRule的key
     */
    public static final String ROUTER_CONFIG_ROUTE_RULE_KEY = "routeRule";

    /**
     * 标签路由key前缀
     */
    public static final String ROUTER_KEY_PREFIX = "servicecomb.routeRule";

    /**
     * dubbo路由规则缓存名
     */
    public static final String DUBBO_CACHE_NAME = "DUBBO_ROUTE";

    /**
     * spring路由规则缓存名
     */
    public static final String SPRING_CACHE_NAME = "SPRING_ROUTE";

    /**
     * dubbo 应用group的key
     */
    public static final String DUBBO_GROUP_KEY = "group";

    /**
     * dubbo 应用version的key
     */
    public static final String DUBBO_VERSION_KEY = "version";

    /**
     * dubbo应用注册标签前缀
     */
    public static final String PARAMETERS_KEY_PREFIX = "service.meta.parameters.";

    /**
     * dubbo应用注册版本的key
     */
    public static final String VERSION_KEY = "service.meta.version";

    private RouterConstant() {
    }
}