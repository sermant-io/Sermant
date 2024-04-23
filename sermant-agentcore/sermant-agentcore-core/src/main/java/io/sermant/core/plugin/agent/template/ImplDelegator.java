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

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

/**
 * ImplDelegator, delegates the implementation of the interface to an implementation instance
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class ImplDelegator {
    /**
     * Interface implementation instance
     */
    private final Object implInstance;

    /**
     * Constructor
     *
     * @param implInstance Interface implementation instance
     */
    public ImplDelegator(Object implInstance) {
        this.implInstance = implInstance;
    }

    /**
     * A delegate method used to implement an interface, a method that proxies a method call to an interface
     * implementation instance
     *
     * @param rawObject raw object
     * @param rawMethod raw method
     * @param args args
     * @return result
     * @throws Exception Exception
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
     * Template class for interface implementation. Inheriting an implementation instance of this class will result
     * in an enhanced raw object
     *
     * @since 2022-01-24
     */
    public static class ImplTemplate {
        /**
         * raw object
         */
        protected Object rawObject;

        /**
         * set raw object
         *
         * @param rawObject raw object
         */
        public void setRawObject(Object rawObject) {
            this.rawObject = rawObject;
        }
    }
}
