/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.service;

import com.huawei.sermant.core.plugin.service.PluginService;

/**
 * 注册服务
 *
 * @author provenceee
 * @since 2021/12/15
 */
public interface RegistryService extends PluginService {
    /**
     * 注册主逻辑
     */
    void startRegistration();

    /**
     * 订阅接口
     *
     * @param url 订阅地址
     * @param notifyListener 实例通知监听器
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    void doSubscribe(Object url, Object notifyListener);

    /**
     * 关闭
     */
    void shutdown();

    /**
     * 增加注册接口
     *
     * @param url 注册url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    void addRegistryUrls(Object url);
}
