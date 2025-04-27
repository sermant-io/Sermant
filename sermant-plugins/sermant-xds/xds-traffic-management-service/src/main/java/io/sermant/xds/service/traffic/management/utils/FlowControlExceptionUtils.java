/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.xds.service.traffic.management.utils;

import io.sermant.xds.common.entity.FlowControlResult;
import io.sermant.xds.service.traffic.management.handler.exception.ExceptionHandlerManager;

/**
 * handle flow control exceptions
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class FlowControlExceptionUtils {
    private static final ExceptionHandlerManager EXCEPTION_HANDLER_MANAGER = new ExceptionHandlerManager();

    private FlowControlExceptionUtils() {
    }

    /**
     * handle flow control exceptions
     *
     * @param throwable exception message
     * @param result pre-return result
     */
    public static void handleException(Throwable throwable, FlowControlResult result) {
        EXCEPTION_HANDLER_MANAGER.apply(throwable, result);
    }
}
