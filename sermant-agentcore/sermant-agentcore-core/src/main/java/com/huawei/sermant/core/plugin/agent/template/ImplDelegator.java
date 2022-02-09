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

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

/**
 * 实现委派器，将接口的实现委派给某个实现实例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class ImplDelegator {
    /**
     * 接口实现实例
     */
    private final Object implInstance;

    public ImplDelegator(Object implInstance) {
        this.implInstance = implInstance;
    }

    /**
     * 用于实现接口的委派方法，将方法调用代理至接口实现实例的方法
     *
     * @param rawObject 原生对象
     * @param rawMethod 原生方法
     * @param args      方法入参
     * @return 方法调用结果
     * @throws Exception 方法执行错误
     */
    @RuntimeType
    public Object impl(@This Object rawObject, @Origin Method rawMethod, @AllArguments Object[] args) throws Exception {
        if (implInstance instanceof ImplTemplate) {
            ((ImplTemplate) implInstance).setRawObject(rawObject);
        }
        return implInstance.getClass().getMethod(rawMethod.getName(), rawMethod.getParameterTypes())
                .invoke(implInstance, args);
    }

    /**
     * 接口实现模板类，继承该类的实现实例将可获得被增强的原生对象
     */
    public static class ImplTemplate {
        /**
         * 原生对象
         */
        protected Object rawObject;

        /**
         * 设置原生对象
         *
         * @param rawObject 原生对象
         */
        public void setRawObject(Object rawObject) {
            this.rawObject = rawObject;
        }
    }
}
