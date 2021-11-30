package com.huawei.javamesh.core.agent.interceptor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.huawei.javamesh.core.config.ConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.javamesh.core.plugin.config.AliaConfig;

/**
 * 拦截器链加载类
 */
public class InterceptorChainManager {

    private static final String MULTI_CHAINS_SEPARATOR = ";";

    private static final String INTERCEPTORS_SEPARATOR = ",";

    private static final Map<String, String> aliaAndNameMap = new HashMap<String, String>();

    private final Map<String, InterceptorChain> interceptorChains = new HashMap<String, InterceptorChain>();

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
}
