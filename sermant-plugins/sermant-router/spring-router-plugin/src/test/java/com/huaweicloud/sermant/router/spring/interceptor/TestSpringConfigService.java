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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

/**
 * 测试配置服务
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class TestSpringConfigService implements SpringConfigService {
    private String cacheName;

    private String serviceName;

    private boolean invalid;

    @Override
    public void init(String cacheName, String serviceName) {
        this.cacheName = cacheName;
        this.serviceName = serviceName;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    public boolean isInValid(String cacheName) {
        return invalid;
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getServiceName() {
        return serviceName;
    }
}