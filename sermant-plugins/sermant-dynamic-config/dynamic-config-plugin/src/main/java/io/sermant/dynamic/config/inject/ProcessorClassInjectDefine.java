/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.dynamic.config.inject;

import io.sermant.core.service.inject.ClassInjectDefine;

/**
 * environment variable configuration
 *
 * @author zhouss
 * @since 2022-04-20
 */
public class ProcessorClassInjectDefine extends DynamicClassInjectDefine {
    @Override
    public String injectClassName() {
        return "io.sermant.dynamic.config.source.SpringEnvironmentProcessor";
    }

    @Override
    public String factoryName() {
        return ENVIRONMENT_PROCESSOR_FACTOR_NAME;
    }

    @Override
    public ClassInjectDefine[] requiredDefines() {
        return new ClassInjectDefine[]{
            this.build("io.sermant.dynamic.config.source.DynamicConfigPropertySource", ""),
            this.build("io.sermant.dynamic.config.source.OriginConfigDisableSource", ""),
            this.build("io.sermant.dynamic.config.closer.ConfigCenterCloser", ""),
            this.build("io.sermant.dynamic.config.closer.NacosConfigCenterCloser", ""),
            this.build("io.sermant.dynamic.config.closer.ZkConfigCenterCloser", ""),
            this.build("io.sermant.dynamic.config.source.OriginConfigCenterDisableListener",
                    ENABLE_AUTO_CONFIGURATION_FACTORY_NAME)
        };
    }
}
