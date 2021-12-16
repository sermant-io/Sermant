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

package com.huawei.sermant.core.service.dynamicconfig.config;

import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.config.common.ConfigFieldKey;
import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigServiceType;

/**
 * Config for this DynamicConfig Module
 */
@ConfigTypeKey("dynamic.config")
public class DynamicConfig implements BaseConfig {
    /**
     * 服务器连接超时时间
     */
    @ConfigFieldKey("timeout_value")
    private int timeoutValue = 30000;

    /**
     * 默认分组
     */
    @ConfigFieldKey("default_group")
    private String defaultGroup = "sermant";

    /**
     * 服务器地址，必须形如：{@code host:port[(,host:port)...]}
     */
    @ConfigFieldKey("server_address")
    private String serverAddress = "127.0.0.1:2181";

    /**
     * 服务实现类型，取NOP、ZOOKEEPER、KIE
     */
    @ConfigFieldKey("dynamic_config_type")
    private DynamicConfigServiceType serviceType = DynamicConfigServiceType.NOP;

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
        return serviceType;
    }
}
