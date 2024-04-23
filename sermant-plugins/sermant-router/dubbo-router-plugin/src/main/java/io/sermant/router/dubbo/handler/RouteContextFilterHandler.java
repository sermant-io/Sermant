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

package io.sermant.router.dubbo.handler;

import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.utils.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * route handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public class RouteContextFilterHandler extends AbstractContextFilterHandler {
    @Override
    public Map<String, List<String>> getRequestTag(Object invoker, Object invocation, Map<String, Object> attachments,
            Set<String> matchKeys, Set<String> injectTags) {
        if (CollectionUtils.isEmpty(matchKeys)) {
            return Collections.emptyMap();
        }
        return getRequestTag(attachments, matchKeys);
    }

    @Override
    public int getOrder() {
        return RouterConstant.ROUTER_HANDLER_ORDER;
    }
}