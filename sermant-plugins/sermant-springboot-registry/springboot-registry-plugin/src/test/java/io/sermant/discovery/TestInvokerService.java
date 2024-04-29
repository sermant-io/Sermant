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

package io.sermant.discovery;

import io.sermant.discovery.entity.DefaultServiceInstance;
import io.sermant.discovery.retry.InvokerContext;
import io.sermant.discovery.retry.config.RetryConfig;
import io.sermant.discovery.service.InvokerService;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * Testing services
 *
 * @author provenceee
 * @since 2023-05-17
 */
public class TestInvokerService implements InvokerService {
    @Override
    public Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc, Function<Throwable, Object> exFunc,
            String serviceName) {
        InvokerContext context = new InvokerContext();
        context.setServiceInstance(new DefaultServiceInstance("127.0.0.1", "127.0.0.1", 8010,
                Collections.emptyMap(), "bar"));
        Object result = invokeFunc.apply(context);
        if (context.getEx() != null) {
            return Optional.ofNullable(exFunc.apply(context.getEx()));
        }
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Object> invoke(Function<InvokerContext, Object> invokeFunc, Function<Throwable, Object> exFunc,
            String serviceName, RetryConfig retryConfig) {
        return Optional.empty();
    }
}