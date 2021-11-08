package com.huawei.apm.core.common;

import com.huawei.apm.core.config.BaseConfig;
import com.huawei.apm.core.config.ConfigTypeKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigTypeKey("configServer")
public class ConfigServerConfig implements BaseConfig {

    private String zkAddress = "127.0.0.1:2181";

    private String zkTimeout = "50000";

    private String zkSleepTime = "1000";

    private String zkRetryTime = "3";
}
