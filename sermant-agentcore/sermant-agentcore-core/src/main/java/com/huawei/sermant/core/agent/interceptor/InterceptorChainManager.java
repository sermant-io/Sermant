/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.agent.interceptor;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.plugin.config.AliaConfig;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拦截器链加载类
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public class InterceptorChainManager {

    private static final String MULTI_CHAINS_SEPARATOR = ";";

    private static final String INTERCEPTORS_SEPARATOR = ",";

    @SuppressWarnings("checkstyle:ConstantName")
    private static final Map<String, String> aliaAndNameMap = new HashMap<String, String>();

    private final Map<String, InterceptorChain> interceptorChains = new HashMap<String, InterceptorChain>();

    public static InterceptorChainManager newInstance() {
        final InterceptorChainManager instance = new InterceptorChainManager();
        final InterceptorChainConfig config = ConfigManager.getConfig(InterceptorChainConfig.class);
        if (config != null) {
            instance.buildChains(config);
        }
        return instance;
    }

    public static void addAlia(AliaConfig pluginAliaConfig) {
        final String pluginName = pluginAliaConfig.getPluginName();
        List<AliaConfig.InterceptorAlia> interceptors = pluginAliaConfig.getInterceptors();
        if (interceptors != null && !interceptors.isEmpty()) {
            for (AliaConfig.InterceptorAlia interceptor : interceptors) {
                aliaAndNameMap.put(pluginName + "." + interceptor.getAlia(), interceptor.getName());
            }
        }
    }

    public InterceptorChain getChain(String interceptorName) {
        return interceptorChains.get(interceptorName);
    }

    private void buildChains(InterceptorChainConfig config) {
        String chainsConfigText = config.getChains();
        if (StringUtils.isBlank(chainsConfigText)) {
            return;
        }
        String[] chainConfigs = chainsConfigText.split(MULTI_CHAINS_SEPARATOR);
        for (String chainConfig : chainConfigs) {
            if (StringUtils.isBlank(chainConfig)) {
                continue;
            }
            buildChain(chainConfig);
        }
    }

    private void buildChain(String chainConfig) {
        String[] interceptorsConfig = chainConfig.split(INTERCEPTORS_SEPARATOR);
        Set<String> interceptors = new LinkedHashSet<String>();
        for (String interceptorOrAlia : interceptorsConfig) {
            if (StringUtils.isBlank(interceptorOrAlia)) {
                continue;
            }
            String interceptor = InterceptorChainManager.aliaAndNameMap.get(interceptorOrAlia);
            if (StringUtils.isBlank(interceptor)) {
                interceptor = interceptorOrAlia;
            }
            interceptors.add(interceptor);
        }
        if (!interceptors.isEmpty()) {
            InterceptorChain interceptorChain = new InterceptorChain(interceptors.toArray(new String[0]));
            for (String interceptor : interceptors) {
                interceptorChains.put(interceptor, interceptorChain);
            }
        }
    }
}
