/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.dubbo.service;

import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;

/**
 * RegistrationInterceptor的service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public class RegistrationServiceImpl implements RegistrationService {
    /**
     * 设置地址-版本缓存
     *
     * @param addr 地址
     * @param version 版本
     */
    @Override
    public void setRegisterVersionCache(String addr, String version) {
        AddrCache.setRegisterVersionCache(addr, version);
    }

    /**
     * 设置自身版本缓存
     *
     * @param version 版本
     */
    @Override
    public void setRegisterVersion(String version) {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(GrayConstant.GRAY_LABEL_CACHE_NAME);
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        currentTag.setRegisterVersion(version);
    }
}