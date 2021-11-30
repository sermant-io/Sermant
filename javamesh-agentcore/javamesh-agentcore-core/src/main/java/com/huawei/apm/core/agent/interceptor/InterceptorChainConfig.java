package com.huawei.apm.core.agent.interceptor;

import com.huawei.apm.core.config.common.BaseConfig;
import com.huawei.apm.core.config.common.ConfigTypeKey;

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
