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

package io.sermant.dubbo.registry.entity;

import io.sermant.dubbo.registry.utils.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * The dubbo interface registers additional data to the registry
 *
 * @author provenceee
 * @since 2022-04-11
 */
public class InterfaceData extends InterfaceKey {
    // This parameter exists from 2.7.11
    private String serviceName;

    // The serial number of the interface is adapted to 2.6.x, 2.7.0-2.7.7
    private Integer order;

    // protocol
    private Set<String> protocol;

    // Additional parameters at the interface level
    private Map<String, String> parameters;

    /**
     * Constructor
     */
    public InterfaceData() {
    }

    /**
     * Constructor
     *
     * @param group Group
     * @param version Version
     * @param serviceName This parameter exists from 2.7.11
     * @param order The serial number of the interface and the serial number of the interface are adapted to 2.6.x,
     * 2.7.0-2.7.7
     *
     * @param parameters Additional parameters at the interface level
     */
    public InterfaceData(String group, String version, String serviceName, Integer order,
            Map<String, String> parameters) {
        super(group, version);
        this.serviceName = serviceName;
        this.order = order;
        this.parameters = CollectionUtils.isEmpty(parameters) ? null : parameters;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Set<String> getProtocol() {
        return protocol;
    }

    public void setProtocol(Set<String> protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
