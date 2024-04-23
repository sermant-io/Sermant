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

package io.sermant.router.config.entity;

import io.sermant.router.config.strategy.ValueMatchStrategy;
import io.sermant.router.config.strategy.match.ExactValueMatchStrategy;
import io.sermant.router.config.strategy.match.GreaterValueMatchStrategy;
import io.sermant.router.config.strategy.match.InValueMatchStrategy;
import io.sermant.router.config.strategy.match.LessValueMatchStrategy;
import io.sermant.router.config.strategy.match.NoEquValueMatchStrategy;
import io.sermant.router.config.strategy.match.NoGreaterValueMatchStrategy;
import io.sermant.router.config.strategy.match.NoLessValueMatchStrategy;
import io.sermant.router.config.strategy.match.PrefixValueMatchStrategy;
import io.sermant.router.config.strategy.match.RegexValueMatchStrategy;

import java.util.List;
import java.util.Locale;

/**
 * Value matching strategy
 *
 * @author provenceee
 * @since 2021-10-14
 */
public enum MatchStrategy {
    /**
     * Equivalence matching
     */
    EXACT(new ExactValueMatchStrategy()),

    /**
     * Regular expression matching
     */
    REGEX(new RegexValueMatchStrategy()),

    /**
     * Not equal to matching
     */
    NOEQU(new NoEquValueMatchStrategy()),

    /**
     * Not less than a match
     */
    NOLESS(new NoLessValueMatchStrategy()),

    /**
     * Not greater than a match
     */
    NOGREATER(new NoGreaterValueMatchStrategy()),

    /**
     * Greater than match
     */
    GREATER(new GreaterValueMatchStrategy()),

    /**
     * 小于匹配
     */
    LESS(new LessValueMatchStrategy()),

    /**
     * 包含匹配
     */
    IN(new InValueMatchStrategy()),

    /**
     * 前缀匹配
     */
    PREFIX(new PrefixValueMatchStrategy());

    private final ValueMatchStrategy valueMatchStrategy;

    MatchStrategy(ValueMatchStrategy valueMatchStrategy) {
        this.valueMatchStrategy = valueMatchStrategy;
    }

    /**
     * 是否匹配
     *
     * @param values 期望值
     * @param arg 参数值
     * @param isCaseInsensitive 是否区分大小写
     * @return 是否匹配
     */
    public boolean isMatch(List<String> values, String arg, boolean isCaseInsensitive) {
        if (isCaseInsensitive || values == null || arg == null) {
            return valueMatchStrategy.isMatch(values, arg);
        }

        // 如果大小写不敏感，则把参数转为小写
        return valueMatchStrategy.isMatch(values, arg.toLowerCase(Locale.ROOT));
    }
}