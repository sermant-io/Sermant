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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 构造方法advice模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class AdviceConstTemplate {
    private AdviceConstTemplate() {
    }

    /**
     * 调用方法的前置触发点
     *
     * @param cls             被增强的类
     * @param constructor     构造函数
     * @param methodKey       方法键，用于查找模板类
     * @param arguments       方法入参
     * @param interceptorMap  拦截器集
     * @param extStaticFields 额外静态属性集
     * @param interceptorItr  拦截器迭代器
     * @param context         执行上下文
     */
    @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:ParameterAssignment"})
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onMethodEnter(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.FieldValue(value = "_INTERCEPTOR_MAP_$SERMANT") Map<String, List<Interceptor>> interceptorMap,
            @Advice.FieldValue(value = "_EXT_STATIC_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extStaticFields,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<Interceptor> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") ExecuteContext context) {
        interceptorItr = interceptorMap.get(methodKey).listIterator();
        context = ExecuteContext.forConstructor(cls, constructor, arguments, extStaticFields);
        context = CommonConstAdviser.onMethodEnter(context, interceptorItr);
        arguments = context.getArguments();
        extStaticFields = context.getExtStaticFields();
    }

    /**
     * 调用方法的后置触发点
     *
     * @param obj             被增强的对象
     * @param extStaticFields 额外静态属性集
     * @param extMemberFields 额外成员属性集
     * @param interceptorItr  拦截器迭代器
     * @param context         执行上下文
     */
    @SuppressWarnings("checkstyle:ParameterAssignment")
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.FieldValue(value = "_EXT_STATIC_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extStaticFields,
            @Advice.FieldValue(value = "_EXT_MEMBER_FIELDS_$SERMANT",
                    readOnly = false, typing = Assigner.Typing.DYNAMIC) Map<String, Object> extMemberFields,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<Interceptor> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") ExecuteContext context) {
        context = CommonConstAdviser.onMethodExit(context.afterConstructor(obj, extMemberFields), interceptorItr);
        extStaticFields = context.getExtStaticFields();
        extMemberFields = context.getExtMemberFields();
    }
}
