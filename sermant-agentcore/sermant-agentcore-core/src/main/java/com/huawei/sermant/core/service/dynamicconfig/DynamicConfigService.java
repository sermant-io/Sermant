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

package com.huawei.sermant.core.service.dynamicconfig;

import java.util.List;

import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.service.BaseService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huawei.sermant.core.service.dynamicconfig.api.GroupService;
import com.huawei.sermant.core.service.dynamicconfig.api.KeyGroupService;
import com.huawei.sermant.core.service.dynamicconfig.api.KeyService;

/**
 * 动态配置服务抽象类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/12/14
 */
public abstract class DynamicConfigService implements BaseService, KeyService, KeyGroupService, GroupService {
    /**
     * 动态配置信息
     */
    protected static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    /**
     * 获取默认组下某个键的配置值
     *
     * @param key 键
     * @return 配置值
     */
    @Override
    public String getConfig(String key) {
        return getConfig(key, CONFIG.getDefaultGroup());
    }

    /**
     * 设置默认组下某个键的配置值
     *
     * @param key     键
     * @param content 配置值
     * @return 是否操作成功
     */
    @Override
    public boolean publishConfig(String key, String content) {
        return publishConfig(key, CONFIG.getDefaultGroup(), content);
    }

    /**
     * 移除默认组下某个键的配置值
     *
     * @param key 键
     * @return 是否操作成功
     */
    @Override
    public boolean removeConfig(String key) {
        return removeConfig(key, CONFIG.getDefaultGroup());
    }

    /**
     * 获取默认组下所有键
     *
     * @return 键集合
     */
    @Override
    public List<String> listKeys() {
        return listKeysFromGroup(CONFIG.getDefaultGroup());
    }

    /**
     * 为默认组下某个键添加监听器
     *
     * @param key      键
     * @param listener 监听器
     * @return 是否操作成功
     */
    @Override
    public boolean addConfigListener(String key, DynamicConfigListener listener) {
        return addConfigListener(key, CONFIG.getDefaultGroup(), listener);
    }

    /**
     * 移除默认组下某个键的监听器
     *
     * @param key 键
     * @return 是否操作成功
     */
    @Override
    public boolean removeConfigListener(String key) {
        return removeConfigListener(key, CONFIG.getDefaultGroup());
    }
}
