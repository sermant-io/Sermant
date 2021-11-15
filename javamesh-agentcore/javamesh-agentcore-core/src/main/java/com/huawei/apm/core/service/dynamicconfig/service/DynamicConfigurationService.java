/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.apm.core.service.dynamicconfig.service;

import com.huawei.apm.core.service.CoreService;


public interface DynamicConfigurationService extends CoreService, AutoCloseable {

    /**
     * {@link #addListener(String, String, ConfigurationListener)}
     *
     * @param key      the key to represent a configuration
     * @param listener configuration listener
     */
    default boolean addListener(String key, ConfigurationListener listener) {
        return addListener(key, getDefaultGroup(), listener);
    }


    /**
     * {@link #removeListener(String, String, ConfigurationListener)}
     *
     * @param key      the key to represent a configuration
     * @param listener configuration listener
     */
    default boolean removeListener(String key, ConfigurationListener listener) {
        return removeListener(key, getDefaultGroup(), listener);
    }

    /**
     * Register a configuration listener for a specified key
     * The listener only works for service governance purpose, so the target group would always be the value user
     * specifies at startup or 'dubbo' by default. This method will only register listener, which means it will not
     * trigger a notification that contains the current value.
     *
     * @param key      the key to represent a configuration
     * @param group    the group where the key belongs to
     * @param listener configuration listener
     */
    boolean addListener(String key, String group, ConfigurationListener listener);

    /**
     * Stops one listener from listening to value changes in the specified key.
     *
     * @param key      the key to represent a configuration
     * @param group    the group where the key belongs to
     * @param listener configuration listener
     */
    boolean removeListener(String key, String group, ConfigurationListener listener);

    /**
     * Get the configuration mapped to the given key and the given group with {@link #getDefaultTimeout() the default
     * timeout}
     *
     * @param key   the key to represent a configuration
     * @param group the group where the key belongs to
     * @return target configuration mapped to the given key and the given group
     */
    String getConfig(String key, String group);

    /**
     * Get the configuration mapped to the given key and the given group with {@link #getDefaultTimeout() the default
     * timeout}
     *
     * @param key   the key to represent a configuration
     * @return target configuration mapped to the given key and the given group
     */
    default String getConfig(String key)
    {
        return getConfig(key, getDefaultGroup());
    }


    /**
     * Publish Config mapped to the given key under the {@link #getDefaultGroup() default group}
     *
     * @param key     the key to represent a configuration
     * @param content the content of configuration
     * @return <code>true</code> if success, or <code>false</code>
     * @throws UnsupportedOperationException If the under layer does not support
     * @since 2.7.5
     */
    default boolean publishConfig(String key, String content) {
        return publishConfig(key, getDefaultGroup(), content);
    }

    /**
     * Publish Config mapped to the given key and the given group.
     *
     * @param key     the key to represent a configuration
     * @param group   the group where the key belongs to
     * @param content the content of configuration
     * @return <code>true</code> if success, or <code>false</code>
     * @throws UnsupportedOperationException If the under layer does not support
     * @since 2.7.5
     */
    boolean publishConfig(String key, String group, String content) ;

    /**
     * Get the default group for the operations
     *
     * @return The default value
     * @since 2.7.5
     */
    String getDefaultGroup();

    /**
     * Get the default timeout for the operations in milliseconds
     *
     * @return The default value
     * @since 2.7.5
     */
    long getDefaultTimeout();

    /**
     * Close the configuration
     *
     * @throws Exception
     * @since 2.7.5
     */
    @Override
    default void close() throws Exception {
        throw new UnsupportedOperationException();
    }


    /**
     * @param key   the key to represent a configuration
     * @param group the group where the key belongs to
     * @return <code>true</code> if success, or <code>false</code>
     * @since 2.7.8
     */
    default boolean removeConfig(String key, String group) throws Exception {
        throw new UnsupportedOperationException();
    }
}
