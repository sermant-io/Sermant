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

package com.huawei.javamesh.core.service.dynamicconfig.service;

import java.util.List;

public interface DynamicConfigurationService extends AutoCloseable {

    default boolean addConfigListener(String key, ConfigurationListener listener) {
        return addConfigListener(key, getDefaultGroup(), listener);
    }

    default boolean addGroupListener(String Group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }


    default boolean removeConfigListener(String key, ConfigurationListener listener) {
        return removeConfigListener(key, getDefaultGroup(), listener);
    }

    default boolean removeGroupListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    boolean addConfigListener(String key, String group, ConfigurationListener listener);

    boolean removeConfigListener(String key, String group, ConfigurationListener listener);




    String getConfig(String key, String group);


    default String getConfig(String key)
    {
        return getConfig(key, getDefaultGroup());
    }


    default boolean publishConfig(String key, String content) {
        return publishConfig(key, getDefaultGroup(), content);
    }


    boolean publishConfig(String key, String group, String content) ;


    String getDefaultGroup();


    long getDefaultTimeout();


    @Override
    default void close() throws Exception {
        throw new UnsupportedOperationException();
    }


    default boolean removeConfig(String key, String group) throws Exception {
        throw new UnsupportedOperationException();
    }

    default List<String> listConfigsFromGroup(String group) throws Exception {
        throw new UnsupportedOperationException();
    }

    default List<String> listConfigsFromConfig(String key) throws Exception {
        return listConfigsFromConfig(key, getDefaultGroup());
    }

    default List<String> listConfigsFromConfig(String key, String group) throws Exception {
        throw new UnsupportedOperationException();
    }

}
