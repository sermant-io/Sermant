/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract Handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractHandler implements Handler {
    /**
     * From the headers, obtain the request token that needs to be transparently transmitted
     *
     * @param headers HTTP request headers
     * @param keys The key of the tag to be obtained
     * @return Request tags
     */
    protected Map<String, List<String>> getRequestTag(Map<String, List<String>> headers, Set<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        for (String headerKey : keys) {
            if (headers.containsKey(headerKey)) {
                map.put(headerKey, headers.get(headerKey));
            }
        }
        return map;
    }
}