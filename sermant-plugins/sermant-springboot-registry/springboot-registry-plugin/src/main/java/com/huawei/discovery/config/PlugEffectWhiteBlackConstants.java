/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.config;

/**
 * 插件生效、日志打印动态配置相关常量
 *
 * @author chengyouling
 * @since 2022-10-9
 */
public class PlugEffectWhiteBlackConstants {

    /**
     * 监听配置key
     */
    public static final String DYNAMIC_CONFIG_LISTENER_KEY = "sermant.plugin.discovery";

    /**
     * 插件生效--策略
     */
    public static final String DYNAMIC_CONFIG_STRATEGY = "strategy";

    /**
     * 插件生效--服务名白名单value
     */
    public static final String DYNAMIC_CONFIG__VALUE = "value";

    /**
     * 策略-所有服务生效
     */
    public static final String STRATEGY_ALL = "all";

    /**
     * 策略-所有服务不生效
     */
    public static final String STRATEGY_NONE = "none";

    /**
     * 策略-白名单服务生效
     */
    public static final String STRATEGY_WHITE = "white";

    /**
     * 策略-黑名单服务不生效
     */
    public static final String STRATEGY_BLACK = "black";

}
