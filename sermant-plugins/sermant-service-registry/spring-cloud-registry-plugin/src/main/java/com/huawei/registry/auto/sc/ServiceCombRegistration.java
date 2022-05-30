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

package com.huawei.registry.auto.sc;

import com.huawei.registry.entity.MicroServiceInstance;

import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * 注册类
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombRegistration implements Registration {
    private final MicroServiceInstance microServiceInstance;

    /**
     * 构造器
     *
     * @param microServiceInstance 实例
     */
    public ServiceCombRegistration(MicroServiceInstance microServiceInstance) {
        this.microServiceInstance = microServiceInstance;
    }

    @Override
    public String getServiceId() {
        return microServiceInstance.getServiceName();
    }

    @Override
    public String getHost() {
        return microServiceInstance.getHost();
    }

    @Override
    public int getPort() {
        return microServiceInstance.getPort();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return URI.create(String.format(Locale.ENGLISH, "http://%s:%s", getHost(), getPort()));
    }

    @Override
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMetadata();
    }

    /**
     * 获取当前实例信息
     *
     * @return 实例信息
     */
    public MicroServiceInstance getMicroServiceInstance() {
        return microServiceInstance;
    }
}
