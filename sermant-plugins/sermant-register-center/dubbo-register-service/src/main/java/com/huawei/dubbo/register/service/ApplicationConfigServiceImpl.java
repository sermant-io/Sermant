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

package com.huawei.dubbo.register.service;

import com.huawei.dubbo.register.config.DubboCache;

import org.apache.dubbo.config.ApplicationConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用配置服务
 *
 * @author provenceee
 * @date 2021/12/31
 */
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    private static final String GRAY_VERSION_KEY = "gray.version";

    /**
     * 设置注册时的服务名
     *
     * @param obj 增强的类
     */
    @Override
    public void getName(Object obj) {
        if (obj instanceof ApplicationConfig) {
            ApplicationConfig config = (ApplicationConfig) obj;
            DubboCache.INSTANCE.setServiceName(config.getName());
            // 灰度插件的版本号优先设置为注册时的版本号
            Map<String, String> versionMap = new HashMap<>();
            versionMap.put(GRAY_VERSION_KEY, DubboCache.INSTANCE.getDubboConfig().getVersion());
            if (config.getParameters() == null) {
                config.setParameters(versionMap);
            } else {
                config.getParameters().putAll(versionMap);
            }
        }
    }
}