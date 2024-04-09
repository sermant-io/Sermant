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

package com.huaweicloud.sermant.router.config.strategy.match;

import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.config.strategy.ValueMatchStrategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regular expression matching strategy
 *
 * @author provenceee
 * @since 2021-10-23
 */
public class RegexValueMatchStrategy implements ValueMatchStrategy {
    private static final Map<String, Pattern> PATTERN_MAP = new ConcurrentHashMap<>();

    @Override
    public boolean isMatch(List<String> values, String arg) {
        try {
            return !CollectionUtils.isEmpty(values) && values.get(0) != null && arg != null
                    && isMatches(values.get(0), arg);
        } catch (PatternSyntaxException ignored) {
            // 正则表达式不符合，返回false
            return false;
        }
    }

    private boolean isMatches(String regex, String value) {
        Pattern pattern = PATTERN_MAP.computeIfAbsent(regex, Pattern::compile);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}