/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.agent.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.huawei.javamesh.core.exception.EnhanceException;

/**
 * 拦截器加载器
 */
public class InterceptorLoader {
    /**
     * 全局拦截器缓存
     * key : 组合{@link InterceptorLoader#generateKey(String, ClassLoader)}
     * value : 拦截器
     */
    private static final Map<String, Interceptor> INTERCEPTOR_CACHE = new ConcurrentHashMap<String, Interceptor>();

    private static final InterceptorChainManager CHAIN_MANAGER = InterceptorChainManager.newInstance();

    private static final InterceptorChain EMPTY_CHAIN = new InterceptorChain(new String[0]);

    public static <T extends Interceptor> List<T> getInterceptors(Collection<String> interceptorsName,
        ClassLoader classLoader, Class<T> interceptorType) {
        final InterceptorChain chainConfig = getInterceptorChain(interceptorsName);

        final List<T> interceptorList = new ArrayList<T>();
        for (String interceptor : interceptorsName) {
            interceptorList.add(getInterceptor(interceptor, classLoader, interceptorType));
        }
        Collections.sort(interceptorList, new Comparator<Interceptor>() {
            @Override
            public int compare(Interceptor o1, Interceptor o2) {
                return chainConfig.getPriority(o1.getClass().getName()) - chainConfig.getPriority(o2.getClass().getName());
            }
        });
        return interceptorList;
    }

    private static InterceptorChain getInterceptorChain(Collection<String> interceptorsName) {
        Set<InterceptorChain> interceptorChains = new HashSet<InterceptorChain>();
        for (String interceptor : interceptorsName) {
            InterceptorChain chain = CHAIN_MANAGER.getChain(interceptor);
            if (chain == null) {
                continue;
            }
            interceptorChains.add(chain);
        }
        if (interceptorChains.size() > 1) {
            throw new EnhanceException("The provided interceptors are not all in the same chain.");
        } else if (interceptorChains.isEmpty()) {
            return EMPTY_CHAIN;
        } else {
            return interceptorChains.iterator().next();
        }
    }

    public static <T extends Interceptor> T getInterceptor(final String interceptor,
        ClassLoader classLoader,
        final Class<T> interceptorType) {
        // classloader不允许为空
        if (classLoader == null) {
            throw new IllegalArgumentException("ClassLoader should not be empty. ");
        }
        String interceptorKey = generateKey(interceptor, classLoader);
        Interceptor cacheInterceptor = INTERCEPTOR_CACHE.get(interceptorKey);
        if (cacheInterceptor == null) {
            cacheInterceptor = newInterceptor(interceptor, classLoader, interceptorType);
            INTERCEPTOR_CACHE.put(interceptorKey, cacheInterceptor);
        }
        // noinspection unchecked
        return (T) cacheInterceptor;
    }

    private static <T extends Interceptor> T newInterceptor(String interceptor,
        ClassLoader classLoader,
        Class<T> interceptorType) {
        try {
            final Class<?> clazz = Class.forName(interceptor, true, classLoader);
            if (interceptorType.isAssignableFrom(clazz)) {
                // noinspection unchecked
                return (T) clazz.newInstance();
            } else {
                throw new EnhanceException("Unmatched interceptor type :[" + interceptor + "].");
            }
        } catch (InstantiationException e) {
            throw new EnhanceException("Instantiation interceptor [" + interceptor + "] failed.");
        } catch (IllegalAccessException e) {
            throw new EnhanceException("Instantiation interceptor [" + interceptor + "] failed.");
        } catch (ClassNotFoundException e) {
            throw new EnhanceException("Cannot find interceptor [" + interceptor + "].");
        }
    }

    private static String generateKey(String interceptor, ClassLoader classLoader) {
        return interceptor + "@" + Integer.toHexString(classLoader.hashCode());
    }
}
