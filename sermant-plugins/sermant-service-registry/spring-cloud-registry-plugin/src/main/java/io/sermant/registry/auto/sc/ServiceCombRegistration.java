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

package io.sermant.registry.auto.sc;

import io.sermant.registry.entity.MicroServiceInstance;

import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * Registration class
 *
 * @author zhouss
 * @since 2022-05-18
 */
public class ServiceCombRegistration implements Registration {
    private final MicroServiceInstance microServiceInstance;

    /**
     * Constructor
     *
     * @param microServiceInstance Instance
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
        return microServiceInstance.isSecure();
    }

    @Override
    public URI getUri() {
        String format = "http://%s:%s";
        if (microServiceInstance.isSecure()) {
            format = "https://%s:%s";
        }
        return URI.create(String.format(Locale.ENGLISH, format, getHost(), getPort()));
    }

    @Override
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMetadata();
    }

    /**
     * Obtain the information about the current instance
     *
     * @return Instance information
     */
    public MicroServiceInstance getMicroServiceInstance() {
        return microServiceInstance;
    }
}
