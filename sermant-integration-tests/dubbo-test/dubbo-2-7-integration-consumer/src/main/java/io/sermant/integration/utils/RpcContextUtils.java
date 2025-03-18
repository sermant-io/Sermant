/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.integration.utils;

import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcContext attachment utils
 *
 * @author chengyouling
 * @since 2024-03-20
 */
public class RpcContextUtils {
    private RpcContextUtils() {

    }

    /**
     * set tags to attachment
     *
     * @param key attachment key
     * @param value attachment value
     */
    public static void setContextTagToAttachment(String key, String value) {
        RpcContext context = RpcContext.getContext();
        try {
            Method clientMethod = context.getClass().getDeclaredMethod("getClientAttachment");
            clientMethod.setAccessible(true);
            ((RpcContext) clientMethod.invoke(context)).setAttachment(key, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            context.setAttachment(key, value);
        }
    }

    /**
     * remove attachment tag
     *
     * @param key attachment key
     */
    public static void removeAttachmentTag(String key) {
        RpcContext context = RpcContext.getContext();
        try {
            Method clientMethod = context.getClass().getDeclaredMethod("getClientAttachment");
            clientMethod.setAccessible(true);
            ((RpcContext) clientMethod.invoke(context)).removeAttachment(key);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            context.removeAttachment(key);
        }
    }
}
