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

package com.huaweicloud.sermant.router.dubbo.strategy.instance;

import com.huaweicloud.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Matching invoker Not in Mismatch
 *
 * @author provenceee
 * @since 2021-12-08
 */
public class MismatchInstanceStrategy extends AbstractInstanceStrategy<Object, List<Map<String, String>>> {
    /**
     * Matching invoker Not in Mismatch
     *
     * @param invoker Invoker
     * @param tags Unmatched tags
     * @param mapper methods to obtain metadata
     * @return whether it matches or not
     */
    @Override
    public boolean isMatch(Object invoker, List<Map<String, String>> tags,
            Function<Object, Map<String, String>> mapper) {
        // Since the tags in mismatch have been matched and have not been matched, they must be eliminated and cannot
        // participate in load balancing, otherwise the traffic ratio will be incorrect (it will be high)
        Map<String, String> metaData = getMetadata(invoker, mapper);
        for (Map<String, String> mismatchTag : tags) {
            if (handleMatch(metaData, mismatchTag)) {
                return false;
            }
        }
        return true;
    }

    private boolean handleMatch(Map<String, String> metaData, Map<String, String> mismatchTag) {
        for (Map.Entry<String, String> entry : mismatchTag.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();
            if (value == null) {
                if (metaData.containsKey(key)) {
                    return true;
                } else {
                    continue;
                }
            }
            if (value.equals(metaData.get(key))) {
                return true;
            }
        }
        return false;
    }
}