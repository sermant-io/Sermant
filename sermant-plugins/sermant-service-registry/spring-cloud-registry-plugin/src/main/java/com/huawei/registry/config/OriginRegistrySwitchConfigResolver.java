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

package com.huawei.registry.config;

/**
 * 双注册场景, 原注册中心心跳开关
 *
 * @author zhouss
 * @since 2022-05-24
 */
public class OriginRegistrySwitchConfigResolver extends RegistryConfigResolver {
    private static final String ORIGIN_REGISTRY_SWITCH_PREFIX = "origin.__registry__.";

    private final RegisterDynamicConfig defaultConfig = new RegisterDynamicConfig();

    @Override
    protected String getConfigPrefix() {
        return ORIGIN_REGISTRY_SWITCH_PREFIX;
    }

    @Override
    protected Object getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    protected Object getOriginConfig() {
        return RegisterDynamicConfig.INSTANCE;
    }
}
