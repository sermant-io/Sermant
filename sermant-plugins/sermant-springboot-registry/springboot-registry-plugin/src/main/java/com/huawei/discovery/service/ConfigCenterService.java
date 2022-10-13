/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service;

import com.huawei.discovery.config.EffectStategyDynamicConfigListener;

import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 插件生效动态配置服务
 *
 * @author chengyouling
 * @since 2022-09-26
 */
public class ConfigCenterService implements PluginService {

    private final AtomicBoolean isRun = new AtomicBoolean();

    /**
     * 实例化动态配置监听
     *
     * @param serviceName
     */
    public void init(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return;
        }
        if (isRun.compareAndSet(false, true)) {
            ConfigSubscriber configSubscriber = new CseGroupConfigSubscriber(
                    serviceName, new EffectStategyDynamicConfigListener(), "serment-springboot-registry");
            configSubscriber.subscribe();
        }
    }
}
