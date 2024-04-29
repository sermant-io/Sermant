/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.tag.transmission.config.strategy;

import java.util.List;

/**
 * Matching strategy interface for keys that need to be transparently transmitted
 *
 * @author lilai
 * @since 2023-09-07
 */
public interface MatchStrategy {
    /**
     * Whether the key in the request or thread variable matches the rule to be transmitted through in the configuration
     *
     * @param key the key that was matched
     * @param keyConfigs key matching configuration
     * @return matching result
     */
    boolean isMatch(String key, List<String> keyConfigs);
}
