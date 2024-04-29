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

package io.sermant.core.plugin.agent.template;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The creator of the method key
 * <p>Specify a particular method in a class, has the same effect as {@code @Advice.Origin("#t\\##m#s")}, see
 * {@link net.bytebuddy.asm.Advice.Origin}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public class MethodKeyCreator {
    private MethodKeyCreator() {
    }

    /**
     * Builds the method key of the constructor
     *
     * @param constructor constructor
     * @return method key
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
     * Build a method key for a method
     *
     * @param method method
     * @return method key
     */
    public static String getMethodKey(Method method) {
        if (method == null) {
            return "#<init>()";
        }
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
     * Build a method key for a MethodDescription
     *
     * @param methodDesc method description
     * @return method key
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
