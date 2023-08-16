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

    private static final int CONNECT_TIMEOUT = 1000;

    private static final int CONNECT_RETRY_TIMES = 5;

    private static final int REQUEST_TIMEOUT = 3000;

    /**
     * 服务器连接超时时间
     */
    @ConfigFieldKey("timeoutValue")
    private int timeoutValue = TIME_OUT_VALUE;

    /**
     * 连接服务器超时时间
     */
    @ConfigFieldKey("connectTimeout")
    private int connectTimeout = CONNECT_TIMEOUT;

    /**
     * 连接服务器重试次数
     */
    @ConfigFieldKey("connectRetryTimes")
    private int connectRetryTimes = CONNECT_RETRY_TIMES;

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

    private String userName;

    private String password;

    private String privateKey;

    private boolean enableAuth = false;

    /**
     * 请求超时时间，nacos每次getConfig需要设置一个超时时间
     */
    @ConfigFieldKey("requestTimeout")
    private int requestTimeout = REQUEST_TIMEOUT;

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

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectRetryTimes() {
        return connectRetryTimes;
    }

    public void setConnectRetryTimes(int connectRetryTimes) {
        this.connectRetryTimes = connectRetryTimes;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isEnableAuth() {
        return enableAuth;
    }

    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}
