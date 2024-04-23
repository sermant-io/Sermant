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

import io.sermant.router.common.handler.Handler;

import java.util.List;
import java.util.Map;

/**
 * route handler interface
 *
 * @author lilai
 * @since 2023-02-24
 */
public interface RouteHandler extends Handler {
    /**
     * 调用路由处理器链
     *
     * @param targetService target service
     * @param invokers invokers
     * @param invocation invocation
     * @param queryMap RegistryDirectory's queryMap
     * @param serviceInterface the name of the interface
     * @return invokers
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invocation
     */
    Object handle(String targetService, List<Object> invokers, Object invocation, Map<String, String> queryMap,
            String serviceInterface);
}
