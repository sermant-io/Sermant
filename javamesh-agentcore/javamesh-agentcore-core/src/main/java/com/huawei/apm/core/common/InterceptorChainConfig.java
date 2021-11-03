package com.huawei.apm.core.common;

import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigTypeKey;

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
