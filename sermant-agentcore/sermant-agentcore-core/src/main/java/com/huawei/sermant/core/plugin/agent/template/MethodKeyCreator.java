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

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 方法键的构建者
 * <p>明确类中的一个特定方法，效果同{@code @Advice.Origin("#t\\##m#s")}，详见{@link net.bytebuddy.asm.Advice.Origin}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public class MethodKeyCreator {
    private MethodKeyCreator() {
    }

    /**
     * 构建构造函数的方法键
     *
     * @param constructor 构造器
     * @return 方法键
     */
    public static String getConstKey(Constructor<?> constructor) {
        final StringBuilder sb = new StringBuilder().append(constructor.getName()).append("#<init>(");
        final Class<?>[] parameters = constructor.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * 构建一个方法的方法键
     *
     * @param method 方法
     * @return 方法键
     */
    public static String getMethodKey(Method method) {
        final StringBuilder sb = new StringBuilder()
                .append(method.getDeclaringClass().getName())
                .append('#')
                .append(method.getName())
                .append('(');
        final Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * 构建一个方法描述的方法键
     *
     * @param methodDesc 方法描述
     * @return 方法键
     */
    public static String getMethodDescKey(MethodDescription.InDefinedShape methodDesc) {
        final StringBuilder sb = new StringBuilder().append(methodDesc.getDeclaringType().asErasure().getTypeName());
        if (methodDesc.isConstructor()) {
            sb.append("#<init>(");
        } else {
            sb.append('#').append(methodDesc.getActualName()).append("(");
        }
        final ParameterList<ParameterDescription.InDefinedShape> parameters = methodDesc.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parameters.get(i).getType().asErasure().getTypeName());
        }
        sb.append(')');
        return sb.toString();
    }
}
