/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.spring.entity.Keys;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * tag handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public class TagHandler extends AbstractHandler {
    /**
     * Obtain transparent tags
     *
     * @param path The path of the request
     * @param methodName http method
     * @param headers HTTP request headers
     * @param parameters URL parameter
     * @param keys The key of the tag to be obtained
     * @return Marks for transparent transmission
     */
    @Override
    public Map<String, List<String>> getRequestTag(String path, String methodName, Map<String, List<String>> headers,
            Map<String, List<String>> parameters, Keys keys) {
        Set<String> matchKeys = keys.getMatchedKeys();
        return getRequestTag(headers, matchKeys);
    }

    @Override
    public int getOrder() {
        return RouterConstant.ROUTER_HANDLER_ORDER;
    }
}