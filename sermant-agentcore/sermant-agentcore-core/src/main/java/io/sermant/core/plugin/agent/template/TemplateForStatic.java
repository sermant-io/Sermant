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

package io.sermant.core.plugin.agent.template;

import io.sermant.core.plugin.agent.adviser.AdviserScheduler;
import io.sermant.core.plugin.agent.entity.ExecuteContext;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

/**
 * Advice template for static method
 *
 * @author luanwenfei
 * @since 2023-07-18
 */
public class TemplateForStatic {
    private TemplateForStatic() {
    }

    /**
     * The preceding trigger point of method
     *
     * @param cls enhanced class
     * @param method the method being enhanced
     * @param methodKey method key, which is used to find template class
     * @param arguments arguments of method
     * @param adviceKey advice class name
     * @param context execute context
     * @param isSkip Whether to skip the main execution of method
     * @return Skip result
     * @throws Throwable execute exception
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onMethodEnter(@Advice.Origin Class<?> cls, @Advice.Origin Method method,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "_ADVICE_KEY_$SERMANT_LOCAL") String adviceKey,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context,
            @Advice.Local(value = "_IS_SKIP_$SERMANT_LOCAL") Boolean isSkip) throws Throwable {
        adviceKey = "TemplateForStatic_" + Integer.toHexString(methodKey.hashCode()) + "_" + cls.getClassLoader();
        context = ExecuteContext.forStaticMethod(cls, method, arguments, null);
        context = AdviserScheduler.onMethodEnter(context, adviceKey);
        arguments = ((ExecuteContext) context).getArguments();
        isSkip = ((ExecuteContext) context).isSkip();
        return isSkip;
    }

    /**
     * The post trigger point of method
     *
     * @param result Method execution result
     * @param throwable Method execution exception
     * @param adviceKey advice class name
     * @param context execute context
     * @param isSkip Whether to skip the main execution of method
     * @throws Throwable execute exception
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown(readOnly = false) Throwable throwable,
            @Advice.Local(value = "_ADVICE_KEY_$SERMANT_LOCAL") String adviceKey,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context,
            @Advice.Local(value = "_IS_SKIP_$SERMANT_LOCAL") Boolean isSkip) throws Throwable {
        context = isSkip ? context : ((ExecuteContext) context).afterMethod(result, throwable);
        context = AdviserScheduler.onMethodExit(context, adviceKey);
        result = ((ExecuteContext) context).getResult();
        if (((ExecuteContext) context).isChangeThrowable()) {
            throwable = ((ExecuteContext) context).getThrowable();
        }
    }
}
