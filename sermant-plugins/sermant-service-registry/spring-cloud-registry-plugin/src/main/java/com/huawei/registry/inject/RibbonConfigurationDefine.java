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

import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine;
import com.huaweicloud.sermant.core.utils.ClassUtils;

/**
 * Ribbon自动配置注入
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class RibbonConfigurationDefine extends BaseAutoConfigurationDefine implements ClassInjectDefine {
    @Override
    public String injectClassName() {
        return "com.huawei.registry.auto.sc.configuration.ServiceCombRibbonAutoConfiguration";
    }

    @Override
    public String factoryName() {
        return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
    }

    @Override
    public ClassInjectDefine[] requiredDefines() {
        return new ClassInjectDefine[]{
                build("com.huawei.registry.auto.sc.ServiceCombServerMetaInfo", null),
                build("com.huawei.registry.auto.sc.ServiceCombServer", null),
                build("com.huawei.registry.auto.sc.ServiceCombServiceList", null),
                build("com.huawei.registry.auto.sc.ServiceCombServerIntrospector", null),
                build("com.huawei.registry.auto.sc.configuration.ServiceCombRibbonConfiguration", null)
        };
    }

    @Override
    public boolean canInject() {
        return super.canInject() && ClassUtils.loadClass(
                "org.springframework.cloud.netflix.ribbon.SpringClientFactory",
                Thread.currentThread().getContextClassLoader()).isPresent();
    }
}
