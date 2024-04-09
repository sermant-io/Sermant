/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.declarer;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InterceptDeclarer
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-27
 */
public abstract class InterceptDeclarer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * constructor
     *
     * @param methodMatcher method matcher
     * @param interceptors interceptors
     * @return InterceptDeclarer
     * @throws IllegalArgumentException IllegalArgumentException
     */
    public static InterceptDeclarer build(MethodMatcher methodMatcher, Interceptor... interceptors) {
        if (methodMatcher == null || interceptors == null || interceptors.length == 0) {
            throw new IllegalArgumentException("Matcher cannot be null and interceptor array cannot be empty. ");
        }
        return new InterceptDeclarer() {
            @Override
            public MethodMatcher getMethodMatcher() {
                return methodMatcher;
            }

            @Override
            public Interceptor[] getInterceptors(ClassLoader classLoader) {
                return interceptors;
            }
        };
    }

    /**
     * build InterceptDeclarer
     *
     * @param methodMatcher method matcher
     * @param interceptors interceptors
     * @return InterceptDeclarer
     * @throws IllegalArgumentException IllegalArgumentException
     * @deprecated Deprecated
     */
    @Deprecated
    public static InterceptDeclarer build(MethodMatcher methodMatcher, String... interceptors) {
        if (methodMatcher == null || interceptors == null || interceptors.length == 0) {
            throw new IllegalArgumentException("Matcher cannot be null and interceptor array cannot be empty. ");
        }
        return new InterceptDeclarer() {
            @Override
            public MethodMatcher getMethodMatcher() {
                return methodMatcher;
            }

            @Override
            public Interceptor[] getInterceptors(ClassLoader classLoader) {
                try {
                    return createInterceptors(interceptors);
                } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                    LOGGER.log(Level.SEVERE,
                            "Unable to create instance of interceptors: " + Arrays.toString(interceptors), e);
                }
                return new Interceptor[0];
            }
        };
    }

    /**
     * Create all interceptor objects using the classLoader of the class being enhanced
     *
     * @param interceptors Interceptor fully qualified name set
     * @return Interceptor set
     * @throws ClassNotFoundException Class not found
     * @throws IllegalAccessException The addURL method or defineClass method cannot be accessed
     * @throws InstantiationException Instantiation failure
     * @deprecated Deprecated
     */
    @Deprecated
    private static Interceptor[] createInterceptors(String[] interceptors)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final ArrayList<Interceptor> interceptorList = new ArrayList<>();
        for (String interceptor : interceptors) {
            final Object instance = ClassLoaderManager.getPluginClassFinder().loadSermantClass(interceptor)
                    .newInstance();
            if (instance instanceof Interceptor) {
                interceptorList.add((Interceptor) instance);
            }
        }
        return interceptorList.toArray(new Interceptor[0]);
    }

    /**
     * Get method matcher
     *
     * @return method matcher
     */
    public abstract MethodMatcher getMethodMatcher();

    /**
     * Gets the interceptor set
     *
     * @param classLoader The classLoader of the enhanced class
     * @return Interceptor set
     */
    public abstract Interceptor[] getInterceptors(ClassLoader classLoader);
}
