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

package com.huaweicloud.sermant.core.plugin.agent.template;

import com.huaweicloud.sermant.core.plugin.agent.transformer.BootstrapTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

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
     * @param cls 被增强的类
     * @param constructor 构造函数
     * @param methodKey 方法键，用于查找模板类
     * @param arguments 方法入参
     * @param interceptorItr 拦截器迭代器
     * @param methodsMap ExecuteContext和CommonMethodAdviser中部方法的反射缓存
     * @param context 执行上下文
     * @throws Exception 执行异常
     */
    @Advice.OnMethodEnter
    public static void onMethodEnter(
            @Advice.Origin Class<?> cls,
            @Advice.Origin Constructor<?> constructor,
            @Advice.Origin("#t\\##m#s") String methodKey,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "_METHODS_MAP_$SERMANT_LOCAL") HashMap<String, Method> methodsMap,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        final String adviceClsName = "com.huaweicloud.sermant.core.plugin.agent.template.BootstrapConstTemplate_"
            + Integer.toHexString(methodKey.hashCode());
        final Class<?> templateCls = loader.loadClass(adviceClsName);
        interceptorItr = ((List<?>)templateCls.getDeclaredField(BootstrapTransformer.INTERCEPTORS_FIELD_NAME).get(null))
            .listIterator();
        methodsMap =
            (HashMap<String, Method>)templateCls.getDeclaredField(BootstrapTransformer.METHODS_MAP_NAME).get(null);
        context = methodsMap.get("forConstructor").invoke(null, cls, constructor, arguments, null);
        context = methodsMap.get("onMethodEnter").invoke(null, context, interceptorItr);
        arguments = (Object[])methodsMap.get("getArguments").invoke(context);
    }

    /**
     * 调用方法的后置触发点
     *
     * @param obj 被增强的对象
     * @param interceptorItr 拦截器迭代器
     * @param methodsMap ExecuteContext和CommonMethodAdviser中部方法的反射缓存
     * @param context 执行上下文
     * @throws Exception 执行异常
     */
    @Advice.OnMethodExit
    public static void onMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Local(value = "_METHODS_MAP_$SERMANT_LOCAL") HashMap<String,Method> methodsMap,
            @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
            @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context
    ) throws Exception {
        context = methodsMap.get("afterConstructor").invoke(context, obj, null);
        methodsMap.get("onMethodExit").invoke(null, context, interceptorItr);
    }
}
