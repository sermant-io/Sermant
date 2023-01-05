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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 注册服务公共参数
 *
 * @author chengyouling
 * @since 2022-11-17
 */
@ConfigTypeKey(value = "register.service")
public class RegisterServiceCommonConfig implements PluginConfig {
    /**
     * 注册中心类型
     */
    private RegisterType registerType = RegisterType.SERVICE_COMB;

    /**
     * SERVICE_COMB注册中心地址，多个地址使用逗号隔开 / NACOS注册中心ip:port
     */
    private String address = "http://127.0.0.1:30100";

    /**
     * 是否加密
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
     * 获取逗号分隔后的地址列表
     *
     * @return 地址列表
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
