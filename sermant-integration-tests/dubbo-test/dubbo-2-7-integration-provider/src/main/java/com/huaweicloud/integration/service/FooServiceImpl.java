/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration.service;

import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 测试接口
 *
 * @author provenceee
 * @since 2022-04-28
 */
public class FooServiceImpl implements FooService {
    @Autowired
    private RegistryConfig registryConfig;

    @Value("${service_meta_zone:${SERVICE_META_ZONE:${service.meta.zone:bar}}}")
    private String zone;

    @Value("${service_meta_version:${SERVICE_META_VERSION:${service.meta.version:1.0.0}}}")
    private String version;

    @Value("${dubbo.application.name}")
    private String name;

    @Value("${service_meta_parameters:${SERVICE_META_PARAMETERS:${service.meta.parameters:}}}")
    private String parameters;

    @Override
    public String foo(String str) {
        return "foo:" + str;
    }

    @Override
    public String foo2(String str) {
        return "foo2:" + str;
    }

    @Override
    public String getRegistryProtocol() {
        return registryConfig.getProtocol();
    }

    @Override
    public String getMetadata(boolean exit) {
        if (exit) {
            System.exit(0);
        }
        return "I'm " + name + ", my version is " + version + ", my zone is " + zone + ", my parameters is ["
            + parameters + "].";
    }
}