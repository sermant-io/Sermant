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

import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.utils.ReflectUtils;
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ApplicationConfig的service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    private final GrayConfig grayConfig;

    /**
     * 构造方法
     */
    public ApplicationConfigServiceImpl() {
        grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
    }

    /**
     * 获取dubbo服务名，并设置灰度参数
     *
     * @param obj 增强的类
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Override
    public void getName(Object obj) {
        String name = ReflectUtils.getName(obj);
        if (StringUtils.isBlank(name)) {
            return;
        }
        DubboCache.INSTANCE.setAppName(name);
        String version = grayConfig.getGrayVersion(GrayConstant.GRAY_DEFAULT_VERSION);
        Map<String, String> versionMap = new HashMap<>();
        versionMap.put(GrayConstant.GRAY_VERSION_KEY, version);
        String ldc = grayConfig.getLdc(GrayConstant.GRAY_DEFAULT_LDC);
        versionMap.put(GrayConstant.GRAY_LDC_KEY, ldc);
        Map<String, String> parameters = ReflectUtils.getParameters(obj);
        if (parameters == null) {
            ReflectUtils.setParameters(obj, versionMap);
        } else {
            parameters.putAll(versionMap);
        }
        GrayConfiguration grayConfiguration = LabelCache.getLabel(GrayConstant.GRAY_LABEL_CACHE_NAME);
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        if (currentTag == null) {
            currentTag = new CurrentTag();
        }
        currentTag.setVersion(version);
        currentTag.setLdc(ldc);
        grayConfiguration.setCurrentTag(currentTag);
    }
}