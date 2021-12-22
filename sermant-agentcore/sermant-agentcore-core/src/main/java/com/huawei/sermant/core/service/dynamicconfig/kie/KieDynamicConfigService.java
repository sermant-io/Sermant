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

package com.huawei.sermant.core.service.dynamicconfig.kie;

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * kie配置中心实现
 * <p></p>
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigService extends DynamicConfigService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static SubscriberManager subscriberManager;

    private final Map<String, List<String>> groupKeyCache = new ConcurrentHashMap<String, List<String>>();

    public KieDynamicConfigService() {
        subscriberManager = new SubscriberManager(CONFIG.getServerAddress());
    }

    @Override
    public boolean removeGroupListener(String group) {
        return updateListener("GroupKey", group, null, false);
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        return updateListener("GroupKey", group, listener, true);
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        return updateListener(key, LabelGroupUtils.createLabelGroup(
                Collections.singletonMap(fixSeparator(group, true), fixSeparator(key, false))),
                listener, true);
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfig(String key, String group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        return subscriberManager.publishConfig(key, group, content);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        return false;
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        return groupKeyCache.get(group);
    }

    /**
     * 更新监听器（删除||添加）
     * 若第一次添加监听器，则会将数据通知给监听器
     *
     * @param key          监听键
     * @param group        分组， 针对KIE特别处理生成group方法{@link LabelGroupUtils#createLabelGroup(Map)}
     * @param listener     对应改组的监听器
     * @param forSubscribe 是否为订阅
     * @return 更新是否成功
     */
    private synchronized boolean updateListener(String key, String group, DynamicConfigListener listener, boolean forSubscribe) {
        updateGroupKey(key, group, forSubscribe);
        try {
            if (forSubscribe) {
                return subscriberManager.addGroupListener(group, listener);
            } else {
                return subscriberManager.removeGroupListener(group, listener);
            }
        } catch (Exception exception) {
            LOGGER.warning("Subscribed kie request failed! raw key : " + key);
            return false;
        }
    }

    private void updateGroupKey(String key, String group, boolean forSubscribe) {
        List<String> keys = groupKeyCache.get(group);
        if (keys == null) {
            keys = new ArrayList<>();
        }
        if (forSubscribe) {
            keys.add(key);
        } else {
            keys.remove(key);
        }
        groupKeyCache.put(group, keys);
    }

    /**
     * 去除路径分隔符
     *
     * @param str key or group
     * @param isGroup 是否为组
     * @return 修正值
     */
    private String fixSeparator(String str, boolean isGroup) {
        if (str == null) {
            if (isGroup) {
                // 默认分组
                str = CONFIG.getDefaultGroup();
            } else {
                throw new IllegalArgumentException("Key must not be empty!");
            }
        }
        return str.startsWith("/") ? str.substring(1) : str;
    }
}
