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
 * Effective policy
 *
 * @author provenceee
 * @since 2022-10-08
 */
public enum Strategy {
    /**
     * All in effect
     */
    ALL((values, serviceName) -> true),

    /**
     * None of them take effect
     */
    NONE((values, serviceName) -> false),

    /**
     * Whitelist policies
     */
    WHITE((values, serviceName) -> !CollectionUtils.isEmpty(values) && values.contains(serviceName)),

    /**
     * Blacklist policies
     */
    BLACK((values, serviceName) -> CollectionUtils.isEmpty(values) || !values.contains(serviceName));

    private final Matcher matcher;

    /**
     * Constructor
     *
     * @param matcher Matcher
     */
    Strategy(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Whether it matches or not
     *
     * @param values Configure the value
     * @param serviceName Service name
     * @return Whether it matches or not
     */
    public boolean isMatch(Collection<String> values, String serviceName) {
        return matcher.isMatch(values, serviceName);
    }

    /**
     * Policy Matcher
     *
     * @author provenceee
     * @since 2022-10-08
     */
    @FunctionalInterface
    interface Matcher {
        /**
         * Whether it matches or not
         *
         * @param values Configure the value
         * @param serviceName Service name
         * @return Whether it matches or not
         */
        boolean isMatch(Collection<String> values, String serviceName);
    }
}