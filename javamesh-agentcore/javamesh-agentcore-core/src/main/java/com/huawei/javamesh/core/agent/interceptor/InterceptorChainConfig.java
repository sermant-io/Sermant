package com.huawei.javamesh.core.agent.interceptor;

import com.huawei.javamesh.core.config.common.BaseConfig;
import com.huawei.javamesh.core.config.common.ConfigTypeKey;

@ConfigTypeKey("interceptor")
public class InterceptorChainConfig implements BaseConfig {

    private String chains;

    public String getChains() {
        return chains;
    }

    public void setChains(String chains) {
        this.chains = chains;
    }
}
