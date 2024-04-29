/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.service.inject.config;

import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.common.ConfigTypeKey;

import java.util.Collections;
import java.util.Set;

/**
 * Class injection service configuration
 *
 * @author luanwenfei
 * @since 2023-08-10
 */
@ConfigTypeKey("inject")
public class InjectConfig implements BaseConfig {
    /**
     * When the class injection service is used, specifying the packages that are indispensable for the injected
     * classes will assist in fetching the classes in those packages through enhancements to class loading
     */
    private Set<String> essentialPackage = Collections.singleton("io.sermant.sermant");

    public Set<String> getEssentialPackage() {
        return essentialPackage;
    }

    public void setEssentialPackage(Set<String> essentialPackage) {
        this.essentialPackage = essentialPackage;
    }
}
