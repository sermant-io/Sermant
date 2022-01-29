/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.sermant.core.plugin.agent.transformer.BootstrapTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 启动类构造函数advice模板
 * <p>启动类加载器加载类的构造函数如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-27
 */
public class BootstrapConstTemplate {
    private BootstrapConstTemplate() {
    }

    /**
     * 调用方法的前置触发点
     *
     * @param cls            被增强的类
     * @param constructor    构造函数
     * @param methodKey      方法键，用于查找模板类
     * @param arguments      方法入参
     * @param contextCls     执行上下文Class
     * @param adviserCls     通用adviser的Class
     * @param interceptorItr 拦截器迭代器
     * @param context        执行上下文
     * @throws Exception 执行异常
     */
    @SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:ParameterAssignment"})
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onMethodEnter(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "_CONTEXT_CLS_$SERMANT_LOCAL") Class<?> contextCls,
            @Advice.Local(value = "_ADVISER_CLS_$SERMANT_LOCAL") Class<?> adviserCls,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        contextCls = loader.loadClass("com.huawei.sermant.core.plugin.agent.entity.ExecuteContext");
        adviserCls = loader.loadClass("com.huawei.sermant.core.plugin.agent.template.CommonConstAdviser");
        final String adviceClsName = "com.huawei.sermant.core.plugin.agent.template.BootstrapConstTemplate_"
                + Integer.toHexString(methodKey.hashCode());
        final Class<?> templateCls = loader.loadClass(adviceClsName);
        interceptorItr = ((List<?>) templateCls.getDeclaredField(BootstrapTransformer.INTERCEPTORS_FIELD_NAME)
                .get(null)).listIterator();
        context = contextCls.getDeclaredMethod("forConstructor", Class.class, Constructor.class, Object[].class,
                Map.class).invoke(null, cls, constructor, arguments, null);
        context = adviserCls.getDeclaredMethod("onMethodEnter", contextCls, ListIterator.class)
                .invoke(null, context, interceptorItr);
        arguments = (Object[]) contextCls.getDeclaredMethod("getArguments").invoke(context);
    }

    /**
     * 调用方法的后置触发点
     *
     * @param obj            被增强的对象
     * @param contextCls     执行上下文Class
     * @param adviserCls     通用adviser的Class
     * @param interceptorItr 拦截器迭代器
     * @param context        执行上下文
     * @throws Exception 执行异常
     */
    @SuppressWarnings("checkstyle:ParameterAssignment")
    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Local(value = "_CONTEXT_CLS_$SERMANT_LOCAL") Class<?> contextCls,
            @Advice.Local(value = "_ADVISER_CLS_$SERMANT_LOCAL") Class<?> adviserCls,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Exception {
        context = contextCls.getDeclaredMethod("afterConstructor", Object.class, Map.class).invoke(context, obj, null);
        adviserCls.getDeclaredMethod("onMethodExit", contextCls, ListIterator.class)
                .invoke(null, context, interceptorItr);
    }
}
