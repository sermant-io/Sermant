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
import com.huawei.route.common.gray.config.GrayConfig;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import org.apache.dubbo.config.ApplicationConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * InterfaceConfigInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
public class InterfaceConfigServiceImpl extends InterfaceConfigService {
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof ApplicationConfig) {
            ApplicationConfig config = (ApplicationConfig) result;
            GrayConfig grayConfig = PluginConfigManager.getPluginConfig(GrayConfig.class);
            String version = grayConfig.getGrayVersion(GrayConstant.GRAY_DEFAULT_VERSION);
            Map<String, String> versionMap = new HashMap<String, String>();
            versionMap.put(GrayConstant.GRAY_VERSION_KEY, version);
            Map<String, String> parameters = config.getParameters();
            if (parameters == null) {
                config.setParameters(versionMap);
                parameters = config.getParameters();
            } else if (parameters.get(GrayConstant.GRAY_VERSION_KEY) == null) {
                config.getParameters().putAll(versionMap);
            }
            String ldc = grayConfig.getLdc(GrayConstant.GRAY_DEFAULT_LDC);
            parameters.put(GrayConstant.GRAY_LDC_KEY, ldc);
            DubboCache.setAppName(config.getName());
            GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
            CurrentTag currentTag = grayConfiguration.getCurrentTag();
            if (currentTag == null) {
                currentTag = new CurrentTag();
            }
            currentTag.setVersion(parameters.get(GrayConstant.GRAY_VERSION_KEY));
            currentTag.setLdc(ldc);
            grayConfiguration.setCurrentTag(currentTag);
        }
        return result;
    }
}