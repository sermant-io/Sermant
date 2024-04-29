/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.dubbo.registry.listener;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;

import java.util.Objects;
import java.util.Set;

/**
 * NACOS Listening Service
 *
 * @since 2022-10-25
 */
public class NacosAggregateListener {
    private final Object notifyListener;
    private final Set<String> serviceNames = new ConcurrentHashSet<>();

    /**
     * Constructor
     *
     * @param notifyListener Listening
     */
    public NacosAggregateListener(Object notifyListener) {
        this.notifyListener = notifyListener;
    }

    /**
     * Set the service name and instance information
     *
     * @param serviceName Service name
     */
    public void saveAndAggregateAllInstances(String serviceName) {
        serviceNames.add(serviceName);
    }

    public Object getNotifyListener() {
        return notifyListener;
    }

    public Set<String> getServiceNames() {
        return serviceNames;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        NacosAggregateListener that = (NacosAggregateListener) object;
        return Objects.equals(notifyListener, that.notifyListener)
                && Objects.equals(serviceNames, that.serviceNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notifyListener, serviceNames);
    }
}
