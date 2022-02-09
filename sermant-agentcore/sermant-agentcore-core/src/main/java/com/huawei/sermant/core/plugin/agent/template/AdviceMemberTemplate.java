/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.agent.template;

import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 成员方法advice模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class AdviceMemberTemplate {
    private AdviceMemberTemplate() {
    }

    /**
     * 调用方法的前置触发点
     *
     * @param obj             被增强对象
     * @param method          被增强的方法
     * @param methodKey       方法键，用于查找模板类
     * @param arguments       方法入参
     * @param interceptorMap  拦截器集
     * @param extStaticFields 额外静态属性集
     * @param extMemberFields 额外成员属性集
     * @param interceptorItr  拦截器迭代器
     * @param context         执行上下文
     * @return 是否跳过主要方法
     */
    @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:ParameterAssignment"})
    @Advice.OnMethodEnter(suppress = Throwable.class, skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onMethodEnter(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Origin Method method,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.FieldValue(value = "_INTERCEPTOR_MAP_$SERMANT") Map<String, List<Interceptor>> interceptorMap,
            @Advice.FieldValue(value = "_EXT_STATIC_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extStaticFields,
            @Advice.FieldValue(value = "_EXT_MEMBER_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extMemberFields,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<Interceptor> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") ExecuteContext context) {
        interceptorItr = interceptorMap.get(methodKey).listIterator();
        context = ExecuteContext.forMemberMethod(obj, method, arguments, extStaticFields, extMemberFields);
        context = CommonMethodAdviser.onMethodEnter(context, interceptorItr);
        arguments = context.getArguments();
        extStaticFields = context.getExtStaticFields();
        extMemberFields = context.getExtMemberFields();
        return context.isSkip();
    }

    /**
     * 调用方法的后置触发点
     *
     * @param result          方法调用结果
     * @param throwable       方法调用异常
     * @param extStaticFields 额外静态属性集
     * @param extMemberFields 额外成员属性集
     * @param interceptorItr  拦截器迭代器
     * @param context         执行上下文
     */
    @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:ParameterAssignment"})
    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown Throwable throwable,
            @Advice.FieldValue(value = "_EXT_STATIC_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extStaticFields,
            @Advice.FieldValue(value = "_EXT_MEMBER_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extMemberFields,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<Interceptor> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") ExecuteContext context) {
        context = context.isSkip() ? context : context.afterMethod(result, throwable);
        context = CommonMethodAdviser.onMethodExit(context, interceptorItr);
        result = context.getResult();
        extStaticFields = context.getExtStaticFields();
        extMemberFields = context.getExtMemberFields();
    }
}
