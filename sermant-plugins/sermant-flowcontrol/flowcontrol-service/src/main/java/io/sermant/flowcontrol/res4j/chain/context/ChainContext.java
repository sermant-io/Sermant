/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.flowcontrol.res4j.chain.context;

import io.sermant.flowcontrol.res4j.chain.HandlerConstants;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain context
 *
 * @author zhouss
 * @since 2022-07-11
 */
public class ChainContext {
    private static final ThreadLocal<Map<String, RequestContext>> THREAD_LOCAL_CONTEXT_MAP = new ThreadLocal<>();

    private static final int MAX_SIZE = 8;

    private ChainContext() {
    }

    /**
     * Gets a cache of thread variables with the specified name
     *
     * @param name name
     * @return ThreadLocalContext
     * @throws IllegalArgumentException Throw an exception when the thread variable exceeds the maximum size
     */
    public static RequestContext getThreadLocalContext(String name) {
        Map<String, RequestContext> contextMap = THREAD_LOCAL_CONTEXT_MAP.get();
        if (contextMap == null) {
            contextMap = new ConcurrentHashMap<>(MAX_SIZE);
            THREAD_LOCAL_CONTEXT_MAP.set(contextMap);
        }
        if (contextMap.size() >= MAX_SIZE) {
            throw new IllegalArgumentException("Can not create context in current thread!");
        }
        return contextMap.computeIfAbsent(name, sourceName -> new RequestContext(THREAD_LOCAL_CONTEXT_MAP, sourceName));
    }

    /**
     * clear thread variable
     */
    public static void remove() {
        final Map<String, RequestContext> contextMap = THREAD_LOCAL_CONTEXT_MAP.get();
        if (contextMap != null) {
            contextMap.clear();
            THREAD_LOCAL_CONTEXT_MAP.remove();
        }
    }

    /**
     * remove specified cache
     *
     * @param name name
     */
    public static void remove(String name) {
        final Map<String, RequestContext> contextMap = THREAD_LOCAL_CONTEXT_MAP.get();
        if (contextMap != null) {
            contextMap.remove(name);
            if (contextMap.isEmpty()) {
                THREAD_LOCAL_CONTEXT_MAP.remove();
            }
        }
    }

    /**
     * add presets for thread variables
     * <p>
     * {@link RequestContext#get(String, Class)} {@link RequestContext#save(String, Object)} {@link
     * RequestContext#remove(String)}
     * </p>
     *
     * @param sourceName source name
     * @param keyPrefix prefix
     */
    public static void setKeyPrefix(String sourceName, String keyPrefix) {
        if (keyPrefix != null) {
            getThreadLocalContext(sourceName).save(HandlerConstants.THREAD_LOCAL_KEY_PREFIX, keyPrefix);
        }
    }

    /**
     * gets the key prefix of the current thread variable
     *
     * @param sourceName source name
     * @return keyPrefix
     */
    public static Optional<String> getKeyPrefix(String sourceName) {
        return Optional.ofNullable(getThreadLocalContext(sourceName)
                .get(HandlerConstants.THREAD_LOCAL_KEY_PREFIX, String.class));
    }
}
