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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * 启动类静态方法advice模板
 * <p>启动类加载器加载类的静态方法如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-27
 */
public class BootstrapStaticTemplate {
    private BootstrapStaticTemplate() {
    }

    /**
     * 调用方法的前置触发点
     *
     * @param cls 被增强的类
     * @param method 被增强的方法
     * @param methodKey 方法键，用于查找模板类
     * @param arguments 方法入参
     * @param methodsMap ExecuteContext和CommonMethodAdviser中部方法的反射缓存
     * @param interceptorItr 拦截器迭代器
     * @param context 执行上下文
     * @param isSkip 是否跳过主流程
     * @return 是否跳过主要方法
     * @throws Exception 执行异常
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onMethodEnter(@Advice.Origin Class<?> cls, @Advice.Origin Method method,
        @Advice.Origin("#t\\##m#s") String methodKey,
        @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
        @Advice.Local(value = "_METHODS_MAP_$SERMANT_LOCAL") HashMap<String, Method> methodsMap,
        @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
        @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context,
        @Advice.Local(value = "_IS_SKIP_$SERMANT_LOCAL") Boolean isSkip) throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        final String adviceClsName = "com.huaweicloud.sermant.core.plugin.agent.template.BootstrapStaticTemplate_"
            + Integer.toHexString(methodKey.hashCode());
        final Class<?> templateCls = loader.loadClass(adviceClsName);
        interceptorItr = ((List<?>)templateCls.getDeclaredField(BootstrapTransformer.INTERCEPTORS_FIELD_NAME).get(null))
            .listIterator();
        methodsMap =
            (HashMap<String, Method>)templateCls.getDeclaredField(BootstrapTransformer.METHODS_MAP_NAME).get(null);
        context = methodsMap.get("forStaticMethod").invoke(null, cls, method, arguments, null);
        context = methodsMap.get("onMethodEnter").invoke(null, context, interceptorItr);
        arguments = (Object[])methodsMap.get("getArguments").invoke(context);
        isSkip = (Boolean)methodsMap.get("isSkip").invoke(context);
        return isSkip;
    }

    /**
     * 调用方法的后置触发点
     *
     * @param result 方法调用结果
     * @param throwable 方法调用异常
     * @param methodsMap ExecuteContext和CommonMethodAdviser中部方法的反射缓存
     * @param interceptorItr 拦截器迭代器
     * @param context 执行上下文
     * @param isSkip 是否跳过主流程
     * @throws Exception 执行异常
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
        @Advice.Thrown Throwable throwable,
        @Advice.Local(value = "_METHODS_MAP_$SERMANT_LOCAL") HashMap<String, Method> methodsMap,
        @Advice.Local(value = "_INTERCEPTOR_ITR_$SERMANT_LOCAL") ListIterator<?> interceptorItr,
        @Advice.Local(value = "_EXECUTE_CONTEXT_$SERMANT_LOCAL") Object context,
        @Advice.Local(value = "_IS_SKIP_$SERMANT_LOCAL") Boolean isSkip) throws Exception {
        context = isSkip ? context : methodsMap.get("afterMethod").invoke(context, result, throwable);
        context = methodsMap.get("onMethodExit").invoke(null, context, interceptorItr);
        result = methodsMap.get("getResult").invoke(context);
    }
}
