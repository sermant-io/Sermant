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

package io.sermant.router.spring.strategy.instance;

import io.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

/**
 * Match instances that are not in mismatch
 *
 * @param <I> Instance generics
 * @author provenceee
 * @since 2021-12-08
 */
public class MismatchInstanceStrategy<I> extends AbstractInstanceStrategy<I, List<Map<String, String>>> {
    /**
     * Match instances that are not in mismatch
     *
     * @param instance Instance
     * @param tags There is no tag on the match
     * @param mapper Methods to obtain metadata
     * @return Whether it matches or not
     */
    @Override
    public boolean isMatch(I instance, List<Map<String, String>> tags, Function<I, Map<String, String>> mapper) {
        // Since the tags in mismatch have been matched and have not been matched, they must be eliminated and cannot
        // participate in load balancing, otherwise the traffic ratio will be incorrect (it will be high)
        Map<String, String> metadata = getMetadata(instance, mapper);
        for (Map<String, String> mismatchTag : tags) {
            if (extracted(metadata, mismatchTag)) {
                return false;
            }
        }
        return true;
    }

    private boolean extracted(Map<String, String> metadata, Map<String, String> mismatchTag) {
        for (Entry<String, String> entry : mismatchTag.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();

            // If the value is null, filter out all tags that contain the tag
            if (value == null) {
                if (metadata.containsKey(key)) {
                    return true;
                } else {
                    continue;
                }
            }
            if (Objects.equals(metadata.get(key), value)) {
                return true;
            }
        }
        return false;
    }
}
