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

import java.lang.reflect.Method;

/**
 * 静态方法advice模板
 *
 * @author luanwenfei
 * @since 2023-07-18
 */
public class TemplateForStatic {
    private TemplateForStatic() {
    }

    /**
     * 调用方法的前置触发点
     *
     * @param cls 被增强的类
     * @param method 被增强的方法
     * @param methodKey 方法键，用于查找模板类
     * @param arguments 方法入参
     * @param adviceKey advice类名
     * @param context 执行上下文
     * @param isSkip 是否跳过主流程
     * @return 是否跳过主要方法
     * @throws Throwable 执行异常
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
     * 调用方法的后置触发点
     *
     * @param result 方法调用结果
     * @param throwable 方法调用异常
     * @param adviceKey advice类名
     * @param context 执行上下文
     * @param isSkip 是否跳过主流程
     * @throws Throwable 执行异常
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown Throwable throwable,
            @Advice.Local(value = "_ADVICE_KEY_$SERMANT_LOCAL") String adviceKey,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context,
            @Advice.Local(value = "_IS_SKIP_$SERMANT_LOCAL") Boolean isSkip) throws Throwable {
        context = isSkip ? context : ((ExecuteContext) context).afterMethod(result, throwable);
        context = AdviserScheduler.onMethodExit(context, adviceKey);
        result = ((ExecuteContext) context).getResult();
    }
}
