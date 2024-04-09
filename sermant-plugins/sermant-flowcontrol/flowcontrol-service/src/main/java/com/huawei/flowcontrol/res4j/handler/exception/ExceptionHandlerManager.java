/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.handler.exception;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.res4j.chain.AbstractChainHandler;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception Handler
 *
 * @author zhouss
 * @since 2022-08-08
 */
public class ExceptionHandlerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private Map<Class<?>, ExceptionHandler<?>> handlers;

    /**
     * constructor
     */
    public ExceptionHandlerManager() {
        loadHandlers();
    }

    /**
     * exceptionHandling
     *
     * @param ex target exception
     * @param result flowcontrol result, The user exception handler changed the response content
     */
    public void apply(Throwable ex, FlowControlResult result) {
        final ExceptionHandler handler = handlers.get(ex.getClass());
        if (handler == null) {
            LOGGER.log(Level.WARNING, "Can not handler flow control exception!", ex);
            return;
        }
        handler.accept(ex, result);
    }

    private void loadHandlers() {
        final HashMap<Class<?>, ExceptionHandler<?>> map = new HashMap<>();
        for (ExceptionHandler<?> handler : ServiceLoader.load(ExceptionHandler.class,
                AbstractChainHandler.class.getClassLoader())) {
            map.put(handler.targetException(), handler);
        }
        handlers = Collections.unmodifiableMap(map);
    }
}
