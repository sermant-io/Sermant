/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.res4j.util;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.res4j.exceptions.InstanceIsolationException;
import com.huawei.flowcontrol.res4j.handler.exception.ExceptionHandlerManager;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * handle flow control exceptions
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class FlowControlExceptionUtils {
    /**
     * exception type of the resource to be released
     */
    private static final Set<Class<?>> RELEASE_FLOW_CONTROL_EXCEPTIONS =
            new HashSet<>(Arrays.asList(RequestNotPermitted.class, CallNotPermittedException.class,
                    InstanceIsolationException.class));

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

    /**
     * Determine whether there is a flow control exception
     *
     * @param throwable exception type
     * @return whether there is a flow control exception
     */
    public static boolean isNeedReleasePermit(Throwable throwable) {
        for (Class<?> clazz : RELEASE_FLOW_CONTROL_EXCEPTIONS) {
            if (clazz.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return false;
    }
}
