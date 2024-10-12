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

package io.sermant.dubbo.registry.entity;

import java.util.Objects;

/**
 * The key to the subscription data
 *
 * @author provenceee
 * @since 2021-12-23
 */
public class SubscriptionKey extends InterfaceKey {
    private final String appId;

    private final String serviceName;

    private final String interfaceName;

    /**
     * Constructor
     *
     * @param appId appId
     * @param serviceName Service name
     * @param interfaceName The name of the interface
     */
    public SubscriptionKey(String appId, String serviceName, String interfaceName) {
        this(appId, serviceName, interfaceName, null, null);
    }

    /**
     * Constructor
     *
     * @param appId appId
     * @param serviceName Service name
     * @param interfaceName The name of the interface
     * @param group Group
     * @param version Version
     */
    public SubscriptionKey(String appId, String serviceName, String interfaceName, String group, String version) {
        super(group, version);
        this.appId = appId;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
    }

    public String getAppId() {
        return appId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            SubscriptionKey that = (SubscriptionKey) obj;
            return Objects.equals(appId, that.appId) && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(interfaceName, that.interfaceName) && super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, serviceName, interfaceName, getGroup(), getVersion());
    }
}
