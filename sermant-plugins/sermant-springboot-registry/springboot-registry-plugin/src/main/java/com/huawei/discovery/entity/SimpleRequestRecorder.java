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

package com.huawei.discovery.entity;

import com.huawei.discovery.config.DiscoveryPluginConfig;
import com.huawei.discovery.utils.HttpConstants;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 简单的请求记录器, 当前仅记录前置请求
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class SimpleRequestRecorder implements Recorder {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicLong allRequestCount = new AtomicLong();

    private final DiscoveryPluginConfig discoveryPluginConfig;

    /**
     * 构造器
     */
    public SimpleRequestRecorder() {
        this.discoveryPluginConfig = PluginConfigManager.getPluginConfig(DiscoveryPluginConfig.class);
    }

    @Override
    public void beforeRequest() {
        final long allRequest = allRequestCount.incrementAndGet();
        if (allRequest <= 0) {
            allRequestCount.set(0);
            LOGGER.info("SimpleRequestRecorder has over the max num of long, it has been reset to 0!");
        }
        if (discoveryPluginConfig.isLoggerFlag()) {
            LOGGER.log(Level.INFO, String.format(Locale.ENGLISH,
                            "currentTime: %s httpClientInterceptor effect count: %s",
                    HttpConstants.currentTime(), allRequest));
        }
    }

    @Override
    public void errorRequest(Throwable ex, long consumeTimeMs) {

    }

    @Override
    public void afterRequest(long consumeTimeMs) {

    }

    @Override
    public void completeRequest() {

    }
}
