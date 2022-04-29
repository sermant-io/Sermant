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

package com.huaweicloud.sermant.core.service.dynamicconfig.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigServiceType;

import java.util.Locale;

/**
 * Config for this DynamicConfig Module
 *
 * @since 2022-01-29
 */
@ConfigTypeKey("dynamic.config")
public class DynamicConfig implements BaseConfig {
    private static final int TIME_OUT_VALUE = 30000;

    /**
     * 服务器连接超时时间
     */
    @ConfigFieldKey("timeoutValue")
    private int timeoutValue = TIME_OUT_VALUE;

    /**
     * 默认分组
     */
    @ConfigFieldKey("defaultGroup")
    private String defaultGroup = "sermant";

    /**
     * 服务器地址，必须形如：{@code host:port[(,host:port)...]}
     */
    @ConfigFieldKey("serverAddress")
    private String serverAddress = "127.0.0.1:2181";

    /**
     * 服务实现类型，取NOP、ZOOKEEPER、KIE
     */
    @ConfigFieldKey("dynamicConfigType")
    private String serviceType = "NOP";

    public int getTimeoutValue() {
        return timeoutValue;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public DynamicConfigServiceType getServiceType() {
        return DynamicConfigServiceType.valueOf(serviceType.toUpperCase(Locale.ROOT));
    }

    public void setTimeoutValue(int timeoutValue) {
        this.timeoutValue = timeoutValue;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
