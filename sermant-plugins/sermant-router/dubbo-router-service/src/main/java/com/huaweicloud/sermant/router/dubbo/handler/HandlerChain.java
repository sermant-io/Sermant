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

package com.huaweicloud.sermant.router.dubbo.handler;

/**
 * 路由处理器链
 *
 * @author lilai
 * @since 2023-02-24
 */
public class HandlerChain extends AbstractRouteHandler {
    private AbstractRouteHandler tail;

    /**
     * 添加路由处理器
     *
     * @param handler 路由处理器
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
