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

package com.huawei.dubbo.registry.interceptor;

import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.dubbo.registry.utils.CollectionUtils;
import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.alibaba.dubbo.common.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enhance the loadRegisteries method of the AbstractInterfaceConfiguration class
 *
 * @author provenceee
 * @since 2022-11-24
 */
public class AlibabaInterfaceConfigInterceptor extends AbstractInterceptor {
    private static final String REGISTRY_PROTOCOL = "registry";

    private final RegisterConfig config;

    /**
     * Constructor
     */
    public AlibabaInterfaceConfigInterceptor() {
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object result = context.getResult();
        if (config.isEnableDubboRegister() && (result instanceof List<?>)) {
            List<URL> urls = (List<URL>) result;
            if (CollectionUtils.isEmpty(urls)) {
                return context;
            }
            if (config.isOpenMigration()) {
                // Just take one
                getScRegistryUrl(urls.get(0)).ifPresent(urls::add);
                return context;
            }
            List<URL> scUrls = new ArrayList<>();
            for (URL url : urls) {
                Optional<URL> registryUrl = getScRegistryUrl(url);
                if (registryUrl.isPresent()) {
                    scUrls.add(registryUrl.get());
                } else {
                    scUrls.add(url);
                }
            }
            context.changeResult(scUrls);
        }
        return context;
    }

    private Optional<URL> getScRegistryUrl(URL url) {
        if (!REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            return Optional.empty();
        }
        if (Constant.SC_REGISTRY_PROTOCOL.equals(url.getParameter(REGISTRY_PROTOCOL))) {
            return Optional.empty();
        }
        return Optional
            .of(url.setAddress(Constant.SC_REGISTRY_IP).addParameter(REGISTRY_PROTOCOL, Constant.SC_REGISTRY_PROTOCOL));
    }
}