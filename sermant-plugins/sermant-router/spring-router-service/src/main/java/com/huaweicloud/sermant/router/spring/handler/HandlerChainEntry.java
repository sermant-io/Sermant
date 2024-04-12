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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.router.common.request.RequestData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Route handler Chain Entry
 *
 * @author lilai
 * @since 2023-02-21
 */
public enum HandlerChainEntry {
    /**
     * Singleton
     */
    INSTANCE;

    private static final int HANDLER_SIZE = 4;

    private static final HandlerChain HANDLER_CHAIN = new HandlerChain();

    static {
        final List<AbstractRouteHandler> handlers = new ArrayList<>(HANDLER_SIZE);
        for (AbstractRouteHandler handler : ServiceLoader.load(AbstractRouteHandler.class,
                HandlerChainEntry.class.getClassLoader())) {
            handlers.add(handler);
        }
        Collections.sort(handlers);
        handlers.forEach(HANDLER_CHAIN::addLastHandler);
    }

    /**
     * Invoke the route handler chain
     *
     * @param targetName Target service name
     * @param instances List of filtered service strengths
     * @param requestData Request data
     * @return Filtered list of instances
     */
    public List<Object> process(String targetName, List<Object> instances, RequestData requestData) {
        return HANDLER_CHAIN.handle(targetName, instances, requestData);
    }
}
