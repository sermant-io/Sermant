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

package com.huawei.route.common.gray.config;

import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.plugin.config.PluginConfig;

import java.util.Map;

/**
 * 灰度配置
 *
 * @author provenceee
 * @since 2021/11/18
 */
@ConfigTypeKey("gray.plugin")
public class GrayConfig implements PluginConfig {
    /**
     * 灰度版本
     */
    private String grayVersion = GrayConstant.GRAY_DEFAULT_VERSION;

    /**
     * ldc
     */
    private String ldc = GrayConstant.GRAY_DEFAULT_LDC;

    /**
     * 自定义key
     */
    private String dubboKey = "gray";

    /**
     * 自定义组名
     */
    private String dubboGroup = "public=default";

    /**
     * 自定义key
     */
    private String springCloudKey = "SPRINGCLOUD_GRAY_LABLE";

    /**
     * 自定义组名
     */
    private String springCloudGroup = "struct=spring";

    /**
     * 其它配置
     */
    private Map<String, String> properties;

    public String getGrayVersion() {
        return grayVersion;
    }

    /**
     * 获取灰度版本
     *
     * @param defaultVersion 默认版本
     * @return 灰度版本
     */
    public String getGrayVersion(String defaultVersion) {
        return grayVersion == null || grayVersion.isEmpty() ? defaultVersion : grayVersion;
    }

    public void setGrayVersion(String grayVersion) {
        this.grayVersion = grayVersion;
    }

    public String getLdc() {
        return ldc;
    }

    /**
     * 获取灰度ldc
     *
     * @param defaultLdc 默认ldc
     * @return ldc
     */
    public String getLdc(String defaultLdc) {
        return ldc == null || ldc.isEmpty() ? defaultLdc : ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }

    public String getDubboKey() {
        return dubboKey;
    }

    public void setDubboKey(String dubboKey) {
        this.dubboKey = dubboKey;
    }

    public String getDubboGroup() {
        return dubboGroup;
    }

    public void setDubboGroup(String dubboGroup) {
        this.dubboGroup = dubboGroup;
    }

    public String getSpringCloudKey() {
        return springCloudKey;
    }

    public void setSpringCloudKey(String springCloudKey) {
        this.springCloudKey = springCloudKey;
    }

    public String getSpringCloudGroup() {
        return springCloudGroup;
    }

    public void setSpringCloudGroup(String springCloudGroup) {
        this.springCloudGroup = springCloudGroup;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
