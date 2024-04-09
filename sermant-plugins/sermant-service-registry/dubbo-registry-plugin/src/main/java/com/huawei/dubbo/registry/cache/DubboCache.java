/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry.cache;

/**
 * Dubbo application name caching
 *
 * @author provenceee
 * @since 2021-12-23
 */
public enum DubboCache {
    /**
     * Singleton caching
     */
    INSTANCE;

    /**
     * Service name
     */
    private String serviceName;

    /**
     * Whether the flag for SC's registered SPI is loaded
     */
    private boolean isLoadSc;

    /**
     * The class of the loaded URL (alibaba/apache)
     */
    private Class<?> urlClass;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Load the sc spi
     */
    public void loadSc() {
        isLoadSc = true;
    }

    public boolean isLoadSc() {
        return isLoadSc;
    }

    public Class<?> getUrlClass() {
        return urlClass;
    }

    public void setUrlClass(Class<?> urlClass) {
        this.urlClass = urlClass;
    }
}
