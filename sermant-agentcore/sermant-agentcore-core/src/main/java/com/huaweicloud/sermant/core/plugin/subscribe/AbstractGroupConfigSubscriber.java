/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.plugin.subscribe;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;
import java.util.Map;

/**
 * 配置订阅
 *
 * @author zhouss
 * @since 2022-04-13
 */
public abstract class AbstractGroupConfigSubscriber implements ConfigSubscriber {
    private final DynamicConfigService dynamicConfigService;

    /**
     * 订阅的插件名称
     */
    private final String pluginName;

    /**
     * 自定义配置实现的构造方法
     *
     * @param dynamicConfigService 配置中心实现
     */
    protected AbstractGroupConfigSubscriber(DynamicConfigService dynamicConfigService) {
        this(dynamicConfigService, null);
    }

    /**
     * 自定义配置实现的构造方法
     *
     * @param dynamicConfigService 配置中心实现
     * @param pluginName 插件名称
     */
    protected AbstractGroupConfigSubscriber(DynamicConfigService dynamicConfigService, String pluginName) {
        if (dynamicConfigService == null) {
            this.dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);
        } else {
            this.dynamicConfigService = dynamicConfigService;
        }
        this.pluginName = pluginName;
    }

    @Override
    public boolean subscribe() {
        if (!isReady()) {
            LoggerFactory.getLogger().warning("The group subscriber is not ready, may be service name is null");
            return false;
        }
        final Map<String, DynamicConfigListener> subscribers = buildGroupSubscribers();
        if (subscribers != null && !subscribers.isEmpty()) {
            for (Map.Entry<String, DynamicConfigListener> entry : subscribers.entrySet()) {
                dynamicConfigService.addGroupListener(entry.getKey(), entry.getValue(), true);
                printSubscribeMsg(entry.getKey());
            }
        }
        return true;
    }

    private void printSubscribeMsg(String group) {
        if (pluginName != null) {
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                    "Plugin [%s] has Success to subscribe group [%s]", pluginName, group));
        } else {
            LoggerFactory.getLogger().info(String.format(Locale.ENGLISH,
                    "Success to subscribe group [%s]", group));
        }
    }

    /**
     * 构建组订阅者
     *
     * @return 订阅全集
     */
    protected abstract Map<String, DynamicConfigListener> buildGroupSubscribers();

    /**
     * 是否可以订阅
     *
     * @return 是否可以订阅
     */
    protected abstract boolean isReady();
}
