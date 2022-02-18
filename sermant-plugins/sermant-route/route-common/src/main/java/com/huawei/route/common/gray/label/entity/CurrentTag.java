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

package com.huawei.route.common.gray.label.entity;

import com.huawei.route.common.gray.label.LabelCache;

/**
 * 当前应用标签
 *
 * @author provenceee
 * @since 2021/10/27
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