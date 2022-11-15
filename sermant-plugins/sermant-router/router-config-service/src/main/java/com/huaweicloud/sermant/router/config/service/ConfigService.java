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

package com.huaweicloud.sermant.router.config.service;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.config.listener.RouterConfigListener;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 配置服务
 *
 * @author provenceee
 * @since 2022-07-14
 */
public abstract class ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicBoolean init = new AtomicBoolean();

    private final RouterConfig routerConfig;

    private final Set<String> requestTags;

    /**
     * 构造方法
     */
    public ConfigService() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        requestTags = new HashSet<>(routerConfig.getRequestTags());
    }

    /**
     * 初始化通知
     *
     * @param cacheName 缓存名
     * @param serviceName 服务名
     */
    public void init(String cacheName, String serviceName) {
        if (StringUtils.isBlank(cacheName) || StringUtils.isBlank(serviceName)) {
            LOGGER.warning(
                String.format(Locale.ROOT, "CacheName[%s] or serviceName[%s] is empty.", cacheName, serviceName));
            return;
        }
        if (init.compareAndSet(false, true)) {
            RouterConfigListener listener = new RouterConfigListener(cacheName);
            ConfigSubscriber subscriber = new CseGroupConfigSubscriber(serviceName, listener, "Sermant-Router");
            subscriber.subscribe();
        }
    }

    /**
     * 获取规则key
     *
     * @return 规则key
     */
    public Set<String> getMatchKeys() {
        return routerConfig.isUseRequestRouter() ? requestTags : RuleUtils.getMatchKeys();
    }
}