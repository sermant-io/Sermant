/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.config;

import com.huawei.apm.core.config.common.ConfigTypeKey;
import com.huawei.apm.core.plugin.config.PluginConfig;
import com.huawei.route.common.gray.constants.GrayConstant;

import java.util.Map;

/**
 * 灰度配置
 *
 * @author pengyuyi
 * @date 2021/11/18
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
     * 自定义标签key
     */
    private String dubboCustomLabel = "public";

    /**
     * 自定义标签value
     */
    private String dubboCustomLabelValue = "default";

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

    public String getDubboCustomLabel() {
        return dubboCustomLabel;
    }

    public void setDubboCustomLabel(String dubboCustomLabel) {
        this.dubboCustomLabel = dubboCustomLabel;
    }

    public String getDubboCustomLabelValue() {
        return dubboCustomLabelValue;
    }

    public void setDubboCustomLabelValue(String dubboCustomLabelValue) {
        this.dubboCustomLabelValue = dubboCustomLabelValue;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
