/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.service.dynamicconfig.api;

import java.util.List;

import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 对同某个键做操作
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public interface KeyService {
    /**
     * 获取某个键的配置值
     *
     * @param key 键
     * @return 配置值
     */
    String getConfig(String key);

    /**
     * 设置某个键的配置值
     *
     * @param key     键
     * @param content 配置值
     * @return 是否操作成功
     */
    boolean publishConfig(String key, String content);

    /**
     * 移除某个键的配置值
     *
     * @param key 键
     * @return 是否操作成功
     */
    boolean removeConfig(String key);

    /**
     * 获取所有键
     *
     * @return 键集合
     */
    List<String> listKeys();

    /**
     * 为某个键添加监听器
     *
     * @param key      键
     * @param listener 监听器
     * @return 是否操作成功
     */
    boolean addConfigListener(String key, DynamicConfigListener listener);

    /**
     * 移除某个键的监听器
     *
     * @param key 键
     * @return 是否操作成功
     */
    boolean removeConfigListener(String key);
}
