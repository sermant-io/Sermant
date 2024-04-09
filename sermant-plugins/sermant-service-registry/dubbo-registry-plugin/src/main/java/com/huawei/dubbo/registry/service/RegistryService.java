/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.plugin.service.PluginService;

/**
 * Registry Service
 *
 * @author provenceee
 * @since 2021-12-15
 */
public interface RegistryService extends PluginService {
    /**
     * Register the main logic
     */
    void startRegistration();

    /**
     * Subscription API
     *
     * @param url Subscription address
     * @param notifyListener instance notifies the listener
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    void doSubscribe(Object url, Object notifyListener);

    /**
     * Shut down
     */
    void shutdown();

    /**
     * The registration interface is added
     *
     * @param url Registration URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    void addRegistryUrls(Object url);

    /**
     * Re-notify URLs when governance data changes
     */
    void notifyGovernanceUrl();
}