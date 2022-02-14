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

import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.utils.ReflectUtils;
import com.huawei.register.config.RegisterConfig;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用配置服务，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021/12/31
 */
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    private static final String GRAY_VERSION_KEY = "gray.version";

    private final RegisterConfig config;

    /**
     * 初始化启动方法
     */
    public ApplicationConfigServiceImpl() {
        config = PluginConfigManager.getPluginConfig(RegisterConfig.class);
    }

    /**
     * 设置注册时的服务名
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
        DubboCache.INSTANCE.setServiceName(name);

        // 灰度插件的版本号优先设置为注册时的版本号
        Map<String, String> versionMap = new HashMap<>();
        versionMap.put(GRAY_VERSION_KEY, config.getVersion());
        Map<String, String> map = ReflectUtils.getParameters(obj);
        if (map == null) {
            ReflectUtils.setParameters(obj, versionMap);
        } else {
            map.putAll(versionMap);
        }
    }
}