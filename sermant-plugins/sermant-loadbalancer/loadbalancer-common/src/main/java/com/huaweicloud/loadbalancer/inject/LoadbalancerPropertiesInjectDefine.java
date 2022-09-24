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

package com.huaweicloud.loadbalancer.inject;

import com.huaweicloud.sermant.core.service.inject.ClassInjectDefine;

/**
 * 配置类注入
 *
 * @author zhouss
 * @since 2022-08-04
 */
public class LoadbalancerPropertiesInjectDefine implements ClassInjectDefine {
    @Override
    public String injectClassName() {
        return "com.huaweicloud.loadbalancer.inject.LoadbalancerProperties";
    }

    @Override
    public String factoryName() {
        return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
    }

    @Override
    public Plugin plugin() {
        return Plugin.LOAD_BALANCER_PLUGIN;
    }
}
