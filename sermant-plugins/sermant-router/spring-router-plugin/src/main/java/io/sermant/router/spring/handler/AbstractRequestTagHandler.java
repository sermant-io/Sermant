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

package io.sermant.router.spring.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Web Blocker handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractRequestTagHandler extends AbstractHandler {
    /**
     * Obtain transparent tags
     *
     * @param path The path of the request
     * @param methodName HTTP method
     * @param headers HTTP request headers
     * @param parameters URL parameter
     * @param keys Transparent transmission is marked with a key
     * @return Marks for transparent transmission
     */
    public abstract Map<String, List<String>> getRequestTag(String path, String methodName,
            Map<String, List<String>> headers, Map<String, String[]> parameters, Keys keys);

    /**
     * Transparent transmission marks the key entity
     *
     * @author provenceee
     * @since 2023-02-21
     */
    public static class Keys {
        private final Set<String> matchKeys;

        private final Set<String> injectTags;

        /**
         * Constructor
         *
         * @param matchKeys Label routing transparent transmission markers
         * @param injectTags Swim lane transparent markers
         */
        public Keys(Set<String> matchKeys, Set<String> injectTags) {
            this.matchKeys = matchKeys;
            this.injectTags = injectTags;
        }

        public Set<String> getMatchKeys() {
            return matchKeys;
        }

        public Set<String> getInjectTags() {
            return injectTags;
        }
    }
}