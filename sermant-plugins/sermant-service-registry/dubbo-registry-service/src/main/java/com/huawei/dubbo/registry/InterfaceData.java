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

package com.huawei.dubbo.registry;

/**
 * dubbo接口注册到注册中心的额外数据
 *
 * @author provenceee
 * @since 2022-04-11
 */
public class InterfaceData extends InterfaceKey {
    // 2.7.11开始存在该参数
    private final String serviceName;

    // 接口的序号，适配2.6.x, 2.7.0-2.7.7
    private final Integer order;

    /**
     * 构造方法
     *
     * @param group 组
     * @param version 版本
     * @param serviceName 2.7.11开始存在该参数
     * @param order 接口的序号，接口的序号，适配2.6.x, 2.7.0-2.7.7
     */
    public InterfaceData(String group, String version, String serviceName, Integer order) {
        super(group, version);
        this.serviceName = serviceName;
        this.order = order;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getOrder() {
        return order;
    }
}