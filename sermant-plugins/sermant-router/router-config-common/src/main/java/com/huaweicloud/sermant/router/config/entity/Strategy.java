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

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.Collection;

/**
 * 生效策略
 *
 * @author provenceee
 * @since 2022-10-08
 */
public enum Strategy {
    /**
     * 全部生效
     */
    ALL((values, serviceName) -> true),

    /**
     * 全不生效
     */
    NONE((values, serviceName) -> false),

    /**
     * 白名单策略
     */
    WHITE((values, serviceName) -> !CollectionUtils.isEmpty(values) && values.contains(serviceName)),

    /**
     * 黑名单策略
     */
    BLACK((values, serviceName) -> CollectionUtils.isEmpty(values) || !values.contains(serviceName));

    private final Matcher matcher;

    /**
     * 构造方法
     *
     * @param matcher 匹配器
     */
    Strategy(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * 是否匹配
     *
     * @param values 配置值
     * @param serviceName 服务名
     * @return 是否匹配
     */
    public boolean isMatch(Collection<String> values, String serviceName) {
        return matcher.isMatch(values, serviceName);
    }

    /**
     * 策略匹配器
     *
     * @author provenceee
     * @since 2022-10-08
     */
    @FunctionalInterface
    interface Matcher {
        /**
         * 是否匹配
         *
         * @param values 配置值
         * @param serviceName 服务名
         * @return 是否匹配
         */
        boolean isMatch(Collection<String> values, String serviceName);
    }
}