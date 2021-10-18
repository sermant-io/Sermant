package com.huawei.apm.premain.enhance;

import com.huawei.apm.bootstrap.config.ConfigLoader;
import com.huawei.apm.premain.config.InterceptorChainConfig;
import com.huawei.apm.premain.plugin.PluginConfig;
import com.huawei.apm.premain.plugin.PluginConfigLoader;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 拦截器链加载类
 */
public class InterceptorChainManager {

    private static final String MULTI_CHAINS_SEPARATOR = ";";

    private static final String INTERCEPTORS_SEPARATOR = ",";

    private static final InterceptorChainManager INSTANCE = new InterceptorChainManager();

    private final Map<String, InterceptorChain> interceptorChains = new HashMap<String, InterceptorChain>();

    private InterceptorChainManager() {
        final InterceptorChainConfig config = ConfigLoader.getConfig(InterceptorChainConfig.class);
        if (config != null) {
            buildChains(config);
        }
    }

    public static InterceptorChainManager getInstance() {
        return INSTANCE;
    }

    public InterceptorChain getChain(String interceptorName) {
        return interceptorChains.get(interceptorName);
    }

    private void buildChains(InterceptorChainConfig config) {
        String chainsConfigText = config.getChains();
        if (StringUtils.isBlank(chainsConfigText)) {
            return;
        }
        final Map<String, String> aliaAndNameMap = getAliaAndNameMap();
        String[] chainConfigs = chainsConfigText.split(MULTI_CHAINS_SEPARATOR);
        for (String chainConfig : chainConfigs) {
            if (StringUtils.isBlank(chainConfig)) {
                continue;
            }
            buildChain(chainConfig, aliaAndNameMap);
        }
    }

    private void buildChain(String chainConfig, Map<String, String> aliaAndNameMap) {
        String[] interceptorsConfig = chainConfig.split(INTERCEPTORS_SEPARATOR);
        Set<String> interceptors = new LinkedHashSet<String>();
        for (String interceptorOrAlia : interceptorsConfig) {
            if (StringUtils.isBlank(interceptorOrAlia)) {
                continue;
            }
            String interceptor = aliaAndNameMap.get(interceptorOrAlia);
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

    private Map<String, String> getAliaAndNameMap() {
        final Map<String, String> aliaAndNameMap = new HashMap<String, String>();
        PluginConfigLoader.foreachPluginConfig(new AliaPluginConfigConsumer(aliaAndNameMap));
        return aliaAndNameMap;
    }

    static class AliaPluginConfigConsumer implements PluginConfigLoader.PluginConfigConsumer {
        private final Map<String, String> aliaAndNameMap;

        AliaPluginConfigConsumer(Map<String, String> aliaAndNameMap) {
            this.aliaAndNameMap = aliaAndNameMap;
        }

        @Override
        public void accept(PluginConfig pluginConfig) {
            final String pluginName = pluginConfig.getPluginName();
            List<PluginConfig.InterceptorAlia> interceptors = pluginConfig.getInterceptors();
            for (PluginConfig.InterceptorAlia interceptor : interceptors) {
                aliaAndNameMap.put(pluginName + "." + interceptor.getAlia(), interceptor.getName());
            }
        }
    }
}
