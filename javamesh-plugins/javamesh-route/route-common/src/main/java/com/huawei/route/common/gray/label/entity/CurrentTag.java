/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.label.entity;

import com.huawei.route.common.gray.label.LabelCache;

/**
 * 当前应用标签
 *
 * @author pengyuyi
 * @date 2021/10/27
 */
public class CurrentTag {
    /**
     * 当前应用版本号
     */
    private String version;

    /**
     * 当前应用注册版本号
     */
    private String registerVersion;

    /**
     * 当前应用ldc
     */
    private String ldc;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getRegisterVersion() {
        return registerVersion;
    }

    public void setRegisterVersion(String registerVersion) {
        this.registerVersion = registerVersion;
    }

    /**
     * 获取生效的版本号
     *
     * @param labelName 缓存的标签名
     * @return 版本号
     */
    public String getValidVersion(String labelName) {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(labelName);
        if (grayConfiguration.getVersionFrom() == VersionFrom.REGISTER_MSG) {
            return registerVersion;
        }
        return version;
    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }
}