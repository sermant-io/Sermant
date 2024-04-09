/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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
 * Constant
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class RouterConstant {
    /**
     * the prefix of the dubbo parameter index
     */
    public static final String DUBBO_SOURCE_TYPE_PREFIX = "args";

    /**
     * the default version of label routing
     */
    public static final String ROUTER_DEFAULT_VERSION = "0.0.0";

    /**
     * isXxx method name prefix
     */
    public static final String IS_METHOD_PREFIX = ".is";

    /**
     * isXxx method name suffix
     */
    public static final String IS_METHOD_SUFFIX = "()";

    /**
     * traffic routing key prefix
     */
    public static final String ROUTER_KEY_PREFIX = "servicecomb.routeRule";

    /**
     * the key of the traffic routing global rule
     */
    public static final String GLOBAL_ROUTER_KEY = "servicecomb.globalRouteRule";

    /**
     * the cache name of the dubbo routing rule
     */
    public static final String DUBBO_CACHE_NAME = "DUBBO_ROUTE";

    /**
     * Spring routing rule cache name
     */
    public static final String SPRING_CACHE_NAME = "SPRING_ROUTE";

    /**
     * Dubbo application group key
     */
    public static final String DUBBO_GROUP_KEY = "group";

    /**
     * The key for Dubbo application version
     */
    public static final String DUBBO_VERSION_KEY = "version";

    /**
     * Dubbo application registration label prefix
     */
    public static final String PARAMETERS_KEY_PREFIX = "service.meta.parameters.";

    /**
     * Key for Dubbo application registration version
     */
    public static final String META_VERSION_KEY = "service.meta.version";

    /**
     * The key to the Dubbo application registration area
     */
    public static final String META_ZONE_KEY = "service.meta.zone";

    /**
     * Match routing types based on traffic
     */
    public static final String FLOW_MATCH_KIND = "routematcher.sermant.io/flow";

    /**
     * the type of route that is matched based on the tag
     */
    public static final String TAG_MATCH_KIND = "routematcher.sermant.io/tag";

    /**
     * The type of staining rule
     */
    public static final String LANE_MATCH_KIND = "route.sermant.io/lane";

    /**
     * Reserved fields that match the same tag first (used in scenarios such as same-AZ priority routing)
     */
    public static final String CONSUMER_TAG = "CONSUMER_TAG";

    /**
     * A list of types that are supported by route matching methods
     */
    public static final List<String> MATCH_KIND_LIST = Arrays.asList(FLOW_MATCH_KIND, TAG_MATCH_KIND, LANE_MATCH_KIND);

    /**
     * The order in which traffic is matched in the handler's chain of responsibility
     */
    public static final int FLOW_HANDLER_ORDER = 1;

    /**
     * The order in which the tag matches in the handler's chain of responsibility
     */
    public static final int TAG_HANDLER_ORDER = 2;

    /**
     * swimlane handler order
     */
    public static final int LANE_HANDLER_ORDER = 100;

    /**
     * route handler order
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
     * Label routing key prefix
     */
    public static final String TAG_KEY_PREFIX = "servicecomb.tagRule";

    /**
     * The key of the global rule for label routing
     */
    public static final String GLOBAL_TAG_KEY = "servicecomb.globalTagRule";

    /**
     * Swim lane key prefix
     */
    public static final String LANE_KEY_PREFIX = "servicecomb.laneRule";

    /**
     * The key of the global swimlane rule
     */
    public static final String GLOBAL_LANE_KEY = "servicecomb.globalLaneRule";

    /**
     * all service level compatible keys
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