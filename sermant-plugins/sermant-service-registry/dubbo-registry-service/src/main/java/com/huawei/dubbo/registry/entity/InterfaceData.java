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

package com.huawei.dubbo.registry.entity;

import com.huawei.dubbo.registry.utils.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * dubbo接口注册到注册中心的额外数据
 *
 * @author provenceee
 * @since 2022-04-11
 */
public class InterfaceData extends InterfaceKey {
    // 2.7.11开始存在该参数
    private String serviceName;

    // 接口的序号，适配2.6.x, 2.7.0-2.7.7
    private Integer order;

    // 协议
    private Set<String> protocol;

    // 接口级的额外参数
    private Map<String, String> parameters;

    /**
     * 构造方法
     */
    public InterfaceData() {
    }

    /**
     * 构造方法
     *
     * @param group 组
     * @param version 版本
     * @param serviceName 2.7.11开始存在该参数
     * @param order 接口的序号，接口的序号，适配2.6.x, 2.7.0-2.7.7
     * @param parameters 接口级的额外参数
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
}