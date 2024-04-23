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

import io.sermant.router.common.request.RequestData;

import java.util.List;

/**
 * Abstract handler chain
 *
 * @author lilai
 * @since 2023-02-21
 */
public abstract class AbstractRouteHandler implements RouteHandler, Comparable<AbstractRouteHandler> {
    private AbstractRouteHandler next;

    @Override
    public List<Object> handle(String targetName, List<Object> instances, RequestData requestData) {
        if (next != null) {
            return next.handle(targetName, instances, requestData);
        }
        return instances;
    }

    @Override
    public int compareTo(AbstractRouteHandler handler) {
        return getOrder() - handler.getOrder();
    }

    public void setNext(AbstractRouteHandler handler) {
        this.next = handler;
    }

    boolean shouldHandle(List<Object> instances) {
        // Routing is only possible if the number of instances is greater than 1
        return instances != null && instances.size() > 1;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
