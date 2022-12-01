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

package com.huawei.dubbo.registry.service.nacos;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.Map;

/**
 * nacos注册服务
 *
 * @author chengyouling
 * @since 2022-10-25
 */
public interface NacosRegistryService extends PluginService {
    /**
     * 注册实例
     *
     * @param url url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    void doRegister(Object url);

    /**
     * 订阅接口
     *
     * @param notifyListener 实例通知监听器
     * @param url url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    void doSubscribe(Object url, Object notifyListener);

    /**
     * 清除实例
     *
     * @param url url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    void doUnregister(Object url);

    /**
     * 构建naming服务
     *
     * @param parameters url参数
     */
    void buildNamingService(Map<String, String> parameters);

    /**
     * 清除订阅
     *
     * @param notifyListener 监听
     * @param url url
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     */
    void doUnsubscribe(Object url, Object notifyListener);

    /**
     * namingService是否可用
     *
     * @return 服务是否可用
     */
    boolean isAvailable();
}