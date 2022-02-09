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
import java.util.logging.Logger;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.service.BaseService;
import com.huawei.sermant.core.service.dynamicconfig.api.GroupService;
import com.huawei.sermant.core.service.dynamicconfig.api.KeyGroupService;
import com.huawei.sermant.core.service.dynamicconfig.api.KeyService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.config.DynamicConfig;

/**
 * 动态配置服务抽象类，对入参进行判空校验，所有空group都会修正为默认group，key和listener则不允许为空
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public abstract class DynamicConfigService implements BaseService, KeyService, KeyGroupService, GroupService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 动态配置信息
     */
    protected static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    /**
     * 修正组，如果组为空，使用默认组替代
     *
     * @param group 组
     * @return 修正后的组
     */
    protected String fixGroup(String group) {
        return group == null || group.length() <= 0 ? CONFIG.getDefaultGroup() : group;
    }

    @Override
    public String getConfig(String key) {
        return getConfig(key, null);
    }

    @Override
    public boolean publishConfig(String key, String content) {
        return publishConfig(key, null, content);
    }

    @Override
    public boolean removeConfig(String key) {
        return removeConfig(key, null);
    }

    @Override
    public List<String> listKeys() {
        return listKeysFromGroup(null);
    }

    @Override
    public boolean addConfigListener(String key, DynamicConfigListener listener) {
        return addConfigListener(key, null, listener);
    }

    @Override
    public boolean removeConfigListener(String key) {
        return removeConfigListener(key, null);
    }

    @Override
    public String getConfig(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return null;
        }
        return doGetConfig(key, fixGroup(group));
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (content == null || content.length() <= 0) {
            LOGGER.warning("Publish empty config is meaningless, so skip. ");
            return false;
        }
        return doPublishConfig(key, fixGroup(group), content);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        return doRemoveConfig(key, fixGroup(group));
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        return doAddConfigListener(key, fixGroup(group), listener);
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        return doRemoveConfigListener(key, fixGroup(group));
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        return doListKeysFromGroup(fixGroup(group));
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        return doAddGroupListener(fixGroup(group), listener);
    }

    @Override
    public boolean removeGroupListener(String group) {
        return doRemoveGroupListener(fixGroup(group));
    }

    /**
     * 为某个键添加监听器(默认组)，根据入参决定是否触发初始化事件
     *
     * @param key      键
     * @param listener 监听器
     * @param ifNotify 是否在添加监听器时响应初始化时间
     * @return 是否操作成功
     */
    public boolean addConfigListener(String key, DynamicConfigListener listener, boolean ifNotify) {
        return addConfigListener(key, null, listener, ifNotify);
    }

    /**
     * 为组下某个键添加监听器，根据入参决定是否触发初始化事件
     *
     * @param key      键
     * @param group    组
     * @param listener 监听器
     * @param ifNotify 是否在添加监听器时响应初始化时间
     * @return 是否操作成功
     */
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        final boolean addResult = addConfigListener(key, group, listener);
        if (addResult && ifNotify) {
            listener.process(DynamicConfigEvent.initEvent(key, fixGroup(group), getConfig(key, group)));
        }
        return addResult;
    }

    /**
     * 为组下所有的键添加监听器，根据入参决定是否触发初始化事件
     *
     * @param group    组名
     * @param listener 监听器
     * @param ifNotify 是否在添加监听器时响应初始化时间
     * @return 是否添加成功
     */
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        final boolean addResult = addGroupListener(group, listener);
        if (ifNotify && addResult) {
            for (String key : listKeysFromGroup(group)) {
                listener.process(DynamicConfigEvent.initEvent(key, fixGroup(group), getConfig(key, group)));
            }
        }
        return addResult;
    }

    /**
     * 获取组下某个键的配置值
     *
     * @param key   键
     * @param group 组
     * @return 配置值
     */
    protected abstract String doGetConfig(String key, String group);

    /**
     * 设置组下某个键的配置值
     *
     * @param key     键
     * @param group   组
     * @param content 配置值
     * @return 是否操作成功
     */
    protected abstract boolean doPublishConfig(String key, String group, String content);

    /**
     * 移除组下某个键的配置值
     *
     * @param key   键
     * @param group 组
     * @return 是否操作成功
     */
    protected abstract boolean doRemoveConfig(String key, String group);

    /**
     * 为组下某个键添加监听器
     *
     * @param key      键
     * @param group    组
     * @param listener 监听器
     * @return 是否操作成功
     */
    protected abstract boolean doAddConfigListener(String key, String group, DynamicConfigListener listener);

    /**
     * 移除组下某个键的监听器
     *
     * @param key   键
     * @param group 组
     * @return 是否操作成功
     */
    protected abstract boolean doRemoveConfigListener(String key, String group);

    /**
     * 获取组中所有键
     *
     * @param group 组名
     * @return 键集合
     */
    protected abstract List<String> doListKeysFromGroup(String group);

    /**
     * 为组下所有的键添加监听器
     *
     * @param group    组名
     * @param listener 监听器
     * @return 是否添加成功
     */
    protected abstract boolean doAddGroupListener(String group, DynamicConfigListener listener);

    /**
     * 移除组下所有键的监听器
     *
     * @param group 组名
     * @return 是否全部移除成功
     */
    protected abstract boolean doRemoveGroupListener(String group);
}
