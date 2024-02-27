/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.service.InvokerRuleStrategyService;
import com.huaweicloud.sermant.router.common.utils.DubboReflectUtils;
import com.huaweicloud.sermant.router.dubbo.mapper.Dubbo3InstanceMapper;

import org.apache.dubbo.registry.client.InstanceAddressURL;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 增强ServiceDiscoveryRegistryDirectory类的overrideWithConfigurator方法
 *
 * @author chengyouling
 * @since 2024-02-20
 */
public class ServiceDiscoveryRegistryDirectoryInterceptor extends AbstractInterceptor {
    private final InvokerRuleStrategyService invokerRuleStrategyService;
    private final AtomicBoolean isInitialized = new AtomicBoolean();

    /**
     * 构造方法
     */
    public ServiceDiscoveryRegistryDirectoryInterceptor() {
        invokerRuleStrategyService = PluginServiceManager.getPluginService(InvokerRuleStrategyService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        Object providerUrl = arguments[0];
        if (providerUrl instanceof InstanceAddressURL) {
            InstanceAddressURL url = (InstanceAddressURL) providerUrl;
            String application = "";
            if (url.getInstance() != null) {
                application = url.getInstance().getServiceName();
            }

            // 保存接口与服务名之间的映射
            DubboCache.INSTANCE.putApplication(DubboReflectUtils.getServiceInterface(arguments[0]), application);
            if (isInitialized.compareAndSet(false, true)) {
                invokerRuleStrategyService.builedDubbo3RuleStrategy(new Dubbo3InstanceMapper());
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}