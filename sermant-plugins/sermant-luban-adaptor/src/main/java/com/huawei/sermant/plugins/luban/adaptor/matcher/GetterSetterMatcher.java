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

package com.huawei.sermant.plugins.luban.adaptor.matcher;

import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Locale;

/**
 * 匹配getter或setter的方法匹配器，仅两者皆存在的情况下匹配成功
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class GetterSetterMatcher extends MethodMatcher {
    @Override
    public boolean matches(MethodDescription methodDescription) {
        if (isSetter(methodDescription)) {
            return getGetter(methodDescription.getActualName(), methodDescription.getDeclaringType()) != null;
        } else if (isGetter(methodDescription)) {
            return getSetter(methodDescription.getActualName(), methodDescription.getDeclaringType()) != null;
        }
        return false;
    }

    /**
     * 获取setter的方法描述
     *
     * @param fieldName      字段名
     * @param typeDefinition 类型定义
     * @return setter的方法描述
     */
    private MethodDescription getSetter(String fieldName, TypeDefinition typeDefinition) {
        for (MethodDescription methodDescription : typeDefinition.getDeclaredMethods()) {
            if (isSetter(methodDescription) && getFieldName(methodDescription.getActualName()).equals(fieldName)) {
                return methodDescription;
            }
        }
        return null;
    }

    /**
     * 获取getter的方法描述
     *
     * @param fieldName      字段名
     * @param typeDefinition 类型定义
     * @return getter的方法描述
     */
    private MethodDescription getGetter(String fieldName, TypeDefinition typeDefinition) {
        for (MethodDescription methodDescription : typeDefinition.getDeclaredMethods()) {
            if (isGetter(methodDescription) && getFieldName(methodDescription.getActualName()).equals(fieldName)) {
                return methodDescription;
            }
        }
        return null;
    }

    /**
     * 判断方法是否为setter
     *
     * @param methodDescription 方法描述
     * @return 是否为setter
     */
    private boolean isSetter(MethodDescription methodDescription) {
        return ElementMatchers.isSetter().matches(methodDescription);
    }

    /**
     * 判断方法是否为getter
     *
     * @param methodDescription 方法描述
     * @return 是否为getter
     */
    private boolean isGetter(MethodDescription methodDescription) {
        return ElementMatchers.isGetter().matches(methodDescription);
    }

    /**
     * 从getter或setter中剥离属性名
     *
     * @param methodName getter或setter名
     * @return 属性名
     */
    private String getFieldName(String methodName) {
        for (String prefix : new String[]{"get", "set", "is"}) {
            if (methodName.startsWith(prefix)) {
                return methodName.substring(prefix.length());
            }
        }
        throw new IllegalArgumentException(String.format(Locale.ROOT,
                "Method %s is not getter or setter. ", methodName));
    }
}
