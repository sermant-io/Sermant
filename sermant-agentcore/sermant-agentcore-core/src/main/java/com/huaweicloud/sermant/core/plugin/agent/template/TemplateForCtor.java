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

package com.huaweicloud.sermant.core.plugin.agent.template;

import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserScheduler;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Constructor;

/**
 * Advice template for constructor
 *
 * @author luanwenfei
 * @since 2023-07-18
 */
public class TemplateForCtor {
    private TemplateForCtor() {
    }

    /**
     * The preceding trigger point of method
     *
     * @param cls enhanced class
     * @param constructor constructor
     * @param methodKey method key, which is used to find template class
     * @param arguments arguments of method
     * @param adviceKey advice class name
     * @param context execute context
     * @throws Throwable execute exception
     */
    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "_ADVICE_KEY_$SERMANT_LOCAL") String adviceKey,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Throwable {
        adviceKey = "TemplateForCtor_" + Integer.toHexString(methodKey.hashCode()) + "_" + cls.getClassLoader();
        context = ExecuteContext.forConstructor(cls, constructor, arguments, null);
        context = AdviserScheduler.onMethodEnter(context, adviceKey);
        arguments = ((ExecuteContext) context).getArguments();
    }

    /**
     * The post trigger point of method
     *
     * @param obj the object being enhanced
     * @param adviceKey advice class name
     * @param context execute context
     * @throws Throwable execute exception
     */
    @Advice.OnMethodExit
    public static void onMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Local(value = "_ADVICE_KEY_$SERMANT_LOCAL") String adviceKey,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Throwable {
        context = ((ExecuteContext) context).afterConstructor(obj, null);
        AdviserScheduler.onMethodExit(context, adviceKey);
    }
}
