/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.service.discovery;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.listener.XdsServiceDiscoveryListener;
import io.sermant.core.utils.CollectionUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * interceptor
 *
 * @author daizhenyu
 * @since 2024-07-05
 **/
public class TemplateInterceptor implements Interceptor {
    private final XdsServiceDiscovery xdsServiceDiscovery;

    private final TemplateConfig templateConfig;

    private List<ServiceInstance> instanceList = new ArrayList<>();

    private SecureRandom secureRandom = new SecureRandom();

    /**
     * constructor
     */
    public TemplateInterceptor() {
        xdsServiceDiscovery = ServiceManager.getService(XdsCoreService.class)
                .getXdsServiceDiscovery();
        templateConfig = PluginConfigManager.getPluginConfig(TemplateConfig.class);
        if (templateConfig.getType().equals("subscribe")) {
            xdsServiceDiscovery.subscribeServiceInstance(templateConfig.getUpstreamServiceName(),
                    new XdsServiceDiscoveryListener() {
                        @Override
                        public void process(Set<ServiceInstance> instances) {
                            instanceList = new ArrayList<>(instances);
                        }
                    });
        }
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!templateConfig.isEnabled()) {
            return context;
        }
        if (templateConfig.getType().equals("get")) {
            instanceList = new ArrayList<>(xdsServiceDiscovery
                    .getServiceInstance(templateConfig.getUpstreamServiceName()));
        }

        context.setLocalFieldValue("serviceCount", instanceList.size());
        if (CollectionUtils.isEmpty(instanceList)) {
            return context;
        }
        int randomIndex = secureRandom.nextInt(instanceList.size());
        ServiceInstance instance = instanceList.get(randomIndex);
        Object[] arguments = context.getArguments();
        arguments[0] = buildUrl(instance);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!templateConfig.isEnabled()) {
            return context;
        }
        String oldResult = (String) context.getResult();
        context.changeResult(oldResult + "-" + context.getLocalFieldValue("serviceCount"));
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    private String buildUrl(ServiceInstance instance) {
        StringBuilder builder = new StringBuilder();
        builder.append(instance.getHost());
        builder.append(":");
        builder.append(instance.getPort());
        return builder.toString();
    }
}
