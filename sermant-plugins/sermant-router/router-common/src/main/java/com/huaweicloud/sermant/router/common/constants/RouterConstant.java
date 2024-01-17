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

import java.util.Arrays;
import java.util.List;

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
     * isXxx方法名前缀
     */
    public static final String IS_METHOD_PREFIX = ".is";

    /**
     * isXxx方法名前缀
     */
    public static final String IS_METHOD_SUFFIX = "()";

    /**
     * 流量路由key前缀
     */
    public static final String ROUTER_KEY_PREFIX = "servicecomb.routeRule";

    /**
     * 流量路由全局规则key
     */
    public static final String GLOBAL_ROUTER_KEY = "servicecomb.globalRouteRule";

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
    public static final String META_VERSION_KEY = "service.meta.version";

    /**
     * dubbo应用注册区域的key
     */
    public static final String META_ZONE_KEY = "service.meta.zone";

    /**
     * 根据流量进行匹配路由的类型
     */
    public static final String FLOW_MATCH_KIND = "routematcher.sermant.io/flow";

    /**
     * 根据tag进行匹配路由的类型
     */
    public static final String TAG_MATCH_KIND = "routematcher.sermant.io/tag";

    /**
     * 染色规则的类型
     */
    public static final String LANE_MATCH_KIND = "route.sermant.io/lane";

    /**
     * 同标签优先匹配的保留字段(用于同AZ优先路由等场景)
     */
    public static final String CONSUMER_TAG = "CONSUMER_TAG";

    /**
     * 路由匹配方式支持的类型列表
     */
    public static final List<String> MATCH_KIND_LIST = Arrays.asList(FLOW_MATCH_KIND, TAG_MATCH_KIND, LANE_MATCH_KIND);

    /**
     * 流量匹配方式在处理器责任链中的顺序
     */
    public static final int FLOW_HANDLER_ORDER = 1;

    /**
     * tag匹配方式在处理器责任链中的顺序
     */
    public static final int TAG_HANDLER_ORDER = 2;

    /**
     * 泳道处理器顺序
     */
    public static final int LANE_HANDLER_ORDER = 100;

    /**
     * 路由处理器顺序
     */
    public static final int ROUTER_HANDLER_ORDER = 200;

    /**
     * -
     */
    public static final String DASH = "-";

    /**
     * .
     */
    public static final String POINT = ".";

    /**
     * version
     */
    public static final String VERSION = "version";

    /**
     * zone
     */
    public static final String ZONE = "zone";

    /**
     * 标签路由key前缀
     */
    public static final String TAG_KEY_PREFIX = "servicecomb.tagRule";

    /**
     * 标签路由全局规则key
     */
    public static final String GLOBAL_TAG_KEY = "servicecomb.globalTagRule";

    /**
     * 泳道key前缀
     */
    public static final String LANE_KEY_PREFIX = "servicecomb.laneRule";

    /**
     * 泳道全局规则key
     */
    public static final String GLOBAL_LANE_KEY = "servicecomb.globalLaneRule";

    /**
     * 全量服务级兼容的key
     */
    public static final List<String> COMPATIBILITY_KEY_LIST = Arrays.asList(ROUTER_KEY_PREFIX, TAG_KEY_PREFIX,
            LANE_KEY_PREFIX);

    /**
     * 全局级兼容的key
     */
    public static final List<String> GLOBAL_COMPATIBILITY_KEY_LIST = Arrays.asList(GLOBAL_ROUTER_KEY, GLOBAL_TAG_KEY,
            GLOBAL_LANE_KEY);

    private RouterConstant() {
    }
}