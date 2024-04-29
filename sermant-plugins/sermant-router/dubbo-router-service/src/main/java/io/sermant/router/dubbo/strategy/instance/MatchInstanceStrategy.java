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

package io.sermant.router.dubbo.strategy.instance;

import io.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.Map;
import java.util.function.Function;

/**
 * Match the invoker with the target version number
 *
 * @author provenceee
 * @since 2021-12-08
 */
public class MatchInstanceStrategy extends AbstractInstanceStrategy<Object, Map<String, String>> {
    /**
     * Match the invoker with the target version number
     *
     * @param invoker Invoker
     * @param tag Matching labels
     * @param mapper Method for obtaining metadata
     * @return whether it matches or not
     */
    @Override
    public boolean isMatch(Object invoker, Map<String, String> tag, Function<Object, Map<String, String>> mapper) {
        Map<String, String> metaData = getMetadata(invoker, mapper);
        for (Map.Entry<String, String> entry : tag.entrySet()) {
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value.equals(metaData.get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }
}