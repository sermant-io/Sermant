/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Register the public parameters of the service
 *
 * @author chengyouling
 * @since 2022-11-17
 */
@ConfigTypeKey(value = "register.service")
public class RegisterServiceCommonConfig implements PluginConfig {
    /**
     * Registry type
     */
    private RegisterType registerType = RegisterType.SERVICE_COMB;

    /**
     * SERVICE_COMB registry address, multiple addresses separated by commas / NACOS registry ip:port
     */
    private String address = "http://127.0.0.1:30100";

    /**
     * Whether it is encrypted or not
     */
    private boolean secure;

    public RegisterType getRegisterType() {
        return registerType;
    }

    public void setRegisterType(RegisterType registerType) {
        this.registerType = registerType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get a comma-separated list of addresses
     *
     * @return List of addresses
     */
    public List<String> getAddressList() {
        if (StringUtils.isBlank(address)) {
            return Collections.emptyList();
        }
        List<String> addressList = new ArrayList<>();
        String[] addressArr = address.split(",");
        for (String item : addressArr) {
            if (!StringUtils.isBlank(item)) {
                addressList.add(item.trim());
            }
        }
        return addressList;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
