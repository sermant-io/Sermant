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

package io.sermant.registry.inject;

import io.sermant.core.service.inject.ClassInjectDefine;

/**
 * Request to intercept the injection
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class RequestInterceptorInjectDefine implements ClassInjectDefine {
    @Override
    public String injectClassName() {
        return "io.sermant.registry.inject.grace.SpringRequestInterceptor";
    }

    @Override
    public String factoryName() {
        return "";
    }

    @Override
    public Plugin plugin() {
        return Plugin.SPRING_REGISTRY_PLUGIN;
    }
}
