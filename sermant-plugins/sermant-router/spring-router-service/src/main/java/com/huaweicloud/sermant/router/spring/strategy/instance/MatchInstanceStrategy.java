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

package com.huaweicloud.sermant.router.spring.strategy.instance;

import com.huaweicloud.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

/**
 * Instances that match the target version number
 *
 * @param <I> Instance generics
 * @author provenceee
 * @since 2021-12-08
 */
public class MatchInstanceStrategy<I> extends AbstractInstanceStrategy<I, Map<String, String>> {
    /**
     * Instances that match the target version number
     *
     * @param instance Instance
     * @param tag Match the tag on
     * @param mapper Methods to obtain metadata
     * @return Whether it matches or not
     */
    @Override
    public boolean isMatch(I instance, Map<String, String> tag, Function<I, Map<String, String>> mapper) {
        Map<String, String> metadata = getMetadata(instance, mapper);
        for (Entry<String, String> entry : tag.entrySet()) {
            if (Objects.equals(metadata.get(entry.getKey()), entry.getValue())) {
                return true;
            }
        }
        return false;
    }
}