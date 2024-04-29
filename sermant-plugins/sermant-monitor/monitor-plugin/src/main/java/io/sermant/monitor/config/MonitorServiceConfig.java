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

package io.sermant.monitor.config;

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

/**
 * configuration class
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
@ConfigTypeKey(value = "monitor.config")
public class MonitorServiceConfig implements PluginConfig {
    /**
     * service switch
     */
    private boolean enableStartService;

    /**
     * performance monitoring address
     */
    private String address;

    /**
     * performance monitoring port
     */
    private int port;

    /**
     * report type
     */
    private String reportType;

    /**
     * user name
     */
    private String userName;

    /**
     * password
     */
    private String password;

    /**
     * secretKey
     */
    private String key;

    public boolean isEnableStartService() {
        return enableStartService;
    }

    public void setEnableStartService(boolean enableStartService) {
        this.enableStartService = enableStartService;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
