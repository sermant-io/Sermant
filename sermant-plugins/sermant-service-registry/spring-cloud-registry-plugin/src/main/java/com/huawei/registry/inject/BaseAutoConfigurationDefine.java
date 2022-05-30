/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.inject;

import com.huawei.registry.config.RegisterConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine;
import com.huaweicloud.sermant.core.utils.ClassUtils;

/**
 * 配置
 *
 * @author zhouss
 * @since 2022-05-19
 */
public abstract class BaseAutoConfigurationDefine implements ClassInjectDefine {
    @Override
    public boolean canInject() {
        final RegisterConfig pluginConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        return pluginConfig.isEnableSpringRegister() && !pluginConfig.isOpenMigration();
    }

    @Override
    public Plugin plugin() {
        return Plugin.SPRING_REGISTRY_PLUGIN;
    }

    /**
     * 指定类是否被当前的类加载器加载
     *
     * @param className 类全限定名
     * @return 是否被加载
     */
    protected boolean isClassExistedOnCurrentClassLoader(String className) {
        return ClassUtils.loadClass(className, Thread.currentThread().getContextClassLoader(), false).isPresent();
    }
}
