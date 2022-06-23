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

/**
 * ServiceComb自动注册发现注入定义
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ScConfigurationInjectDefine extends BaseAutoConfigurationDefine implements ClassInjectDefine {
    @Override
    public String injectClassName() {
        return "com.huawei.registry.auto.sc.configuration.ServiceCombAutoDiscoveryConfiguration";
    }

    @Override
    public String factoryName() {
        return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
    }

    @Override
    public ClassInjectDefine[] requiredDefines() {
        return new ClassInjectDefine[]{
                build("com.huawei.registry.auto.sc.ServiceCombRegistration", null),
                build("com.huawei.registry.auto.sc.ServiceCombAutoRegistration", null),
                build("com.huawei.registry.auto.sc.ServiceCombServiceInstance", null),
                build("com.huawei.registry.auto.sc.ServiceCombDiscoveryClient", null),
                build("com.huawei.registry.auto.sc.ServiceInstanceHolder", null),
                build("com.huawei.registry.auto.sc.ServiceCombRegistry", null),
                build("com.huawei.registry.auto.sc.ServiceCombHealthIndicator", null, () ->
                        isClassExistedOnCurrentClassLoader(
                                "org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator")),
                buildReactiveDefine()
        };
    }

    private ClassInjectDefine buildReactiveDefine() {
        return new ClassInjectDefine() {
            @Override
            public String injectClassName() {
                return "com.huawei.registry.auto.sc.reactive.ServiceCombReactiveConfiguration";
            }

            @Override
            public String factoryName() {
                return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
            }

            @Override
            public boolean canInject() {
                return isClassExistedOnCurrentClassLoader("org.springframework.cloud.client.discovery.composite"
                                + ".reactive.ReactiveCompositeDiscoveryClientAutoConfiguration");
            }

            @Override
            public ClassInjectDefine[] requiredDefines() {
                return new ClassInjectDefine[] {
                        build("com.huawei.registry.auto.sc.reactive.ServiceCombReactiveDiscoveryClient",
                                null, () -> isClassExistedOnCurrentClassLoader(
                                        "org.springframework.cloud.client.discovery.ReactiveDiscoveryClient"))
                };
            }
        };
    }
}
