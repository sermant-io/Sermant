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

import io.sermant.router.common.handler.Handler;
import io.sermant.router.common.request.RequestData;

import java.util.List;

/**
 * Route handler Interface
 *
 * @author lilai
 * @since 2023-02-21
 */
public interface RouteHandler extends Handler {
    /**
     * Invoke the route handler chain
     *
     * @param targetName Target service name
     * @param instances List of filtered service strengths
     * @param requestData Request data
     * @return Filtered list of instances
     */
    List<Object> handle(String targetName, List<Object> instances, RequestData requestData);
}
