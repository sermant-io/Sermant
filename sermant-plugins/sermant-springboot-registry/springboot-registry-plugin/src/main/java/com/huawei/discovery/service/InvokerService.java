/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.discovery.service;

import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.retry.config.RetryConfig;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.Optional;
import java.util.function.Function;

/**
 * Method invocation, which is used by the service to take over the call interception point, determines the call logic
 *
 * @author zhouss
 * @since 2022-09-28
 */
public interface InvokerService extends PluginService {
    /**
     * Method calls
     *
     * @param invokeFunc Method caller, which needs to return the result of the actual call
     * @param exFunc Exception wrapper
     * @param serviceName Target service name
     * @return Final response results
     */
    Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc,
            Function<Throwable, Object> exFunc, String serviceName);

    /**
     * Make calls based on custom retrievers
     *
     * @param invokeFunc Method caller, which needs to return the result of the actual call
     * @param exFunc Exception wrapper
     * @param serviceName Target service name
     * @param retryConfig Custom retry configurations
     * @return Final response results
     */
    Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc,
            Function<Throwable, Object> exFunc, String serviceName, RetryConfig retryConfig);
}
