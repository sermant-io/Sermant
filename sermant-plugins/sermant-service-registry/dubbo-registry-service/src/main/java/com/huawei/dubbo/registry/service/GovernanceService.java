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

package com.huawei.dubbo.registry.service;

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.listener.GovernanceConfigListener;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * dubbo url参数
 *
 * @author provenceee
 * @since 2022-04-21
 */
public class GovernanceService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final AtomicBoolean INIT = new AtomicBoolean();

    /**
     * 启动初始化任务
     */
    public void doStart() {
        if (INIT.compareAndSet(false, true)) {
            run();
        }
    }

    private void run() {
        ConfigSubscriber subscriber = new CseGroupConfigSubscriber(DubboCache.INSTANCE.getServiceName(),
            new GovernanceConfigListener(), "DubboRegistry");
        if (subscriber.subscribe()) {
            LOGGER.info("[Dubbo governance data] Subscribe config center successfully!");
        } else {
            LOGGER.severe("[Dubbo governance data] Subscribe config center failed!");
        }
    }
}
