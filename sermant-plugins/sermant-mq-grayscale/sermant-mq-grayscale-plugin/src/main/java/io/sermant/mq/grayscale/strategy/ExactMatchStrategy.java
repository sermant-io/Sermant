/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.strategy;

import io.sermant.core.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * exact matching strategy
 *
 * @author chengyouling
 * @since 2024-06-03
 */
public class ExactMatchStrategy implements MatchStrategy {
    @Override
    public String getMatchTag(Map<String, String> properties, List<String> keyConfigs) {
        int index = 0;
        int step = 2;
        while (index < keyConfigs.size() - 1) {
            if (!StringUtils.isEmpty(properties.get(keyConfigs.get(index)))
                    && properties.get(keyConfigs.get(index)).equals(keyConfigs.get(index + 1))) {
                return keyConfigs.get(index) + "%" + keyConfigs.get(index + 1);
            }
            index += step;
        }
        return "";
    }
}
