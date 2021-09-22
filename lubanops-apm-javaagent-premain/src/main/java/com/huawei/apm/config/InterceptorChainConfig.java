package com.huawei.apm.config;

import com.huawei.apm.bootstrap.config.BaseConfig;
import com.huawei.apm.bootstrap.config.ConfigTypeKey;

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
