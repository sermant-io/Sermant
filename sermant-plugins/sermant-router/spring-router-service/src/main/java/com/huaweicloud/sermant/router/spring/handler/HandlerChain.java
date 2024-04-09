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

/**
 * Route handler Chain
 *
 * @author lilai
 * @since 2023-02-21
 */
public class HandlerChain extends AbstractRouteHandler {
    private AbstractRouteHandler tail;

    /**
     * Add a route handler
     *
     * @param handler Route handler
     */
    public void addLastHandler(AbstractRouteHandler handler) {
        if (tail == null) {
            tail = handler;
            setNext(handler);
            return;
        }
        tail.setNext(handler);
        tail = handler;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
