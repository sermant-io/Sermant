/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dubbo caching
 *
 * @author provenceee
 * @since 2021-11-03
 */
public enum DubboCache {
    /**
     * Instance
     */
    INSTANCE;

    // the name of the dubbo app
    private String appName;

    // Add the version number and route label to the parameters
    private Map<String, String> parameters;

    private final Map<String, String> applicationCache;

    DubboCache() {
        applicationCache = new ConcurrentHashMap<>();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * The relationship between the cache interface and the service name
     *
     * @param interfaceName the name of the interface
     * @param application service name
     */
    public void putApplication(String interfaceName, String application) {
        applicationCache.put(interfaceName, application);
    }

    /**
     * obtain the app name
     *
     * @param serviceInterface interface
     * @return the name of the app
     */
    public String getApplication(String serviceInterface) {
        return applicationCache.get(serviceInterface);
    }

    /**
     * clear applicationCache
     *
     */
    public void clear() {
        applicationCache.clear();
    }
}
