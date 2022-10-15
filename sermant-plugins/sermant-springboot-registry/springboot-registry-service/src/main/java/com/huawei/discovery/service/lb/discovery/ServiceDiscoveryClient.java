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

package com.huawei.discovery.service.lb.discovery;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.ex.QueryInstanceException;

import java.io.Closeable;
import java.util.Collection;

/**
 * 客户端
 *
 * @author zhouss
 * @since 2022-09-26
 */
public interface ServiceDiscoveryClient extends Closeable {
    /**
     * 初始化
     */
    void init();

    /**
     * 注册方法
     *
     * @param serviceInstance 注册
     * @return true注册成功
     */
    boolean registry(ServiceInstance serviceInstance);

    /**
     * 查询实例列表
     *
     * @param serviceId 服务名
     * @return 实例列表
     * @throws QueryInstanceException 查询实例出现问题抛出
     */
    Collection<ServiceInstance> getInstances(String serviceId) throws QueryInstanceException;

    /**
     * 查询所有服务名
     *
     * @return 所有服务名列表
     */
    Collection<String> getServices();

    /**
     * 当前实例下线
     *
     * @return 是否注册成功
     */
    boolean unRegistry();

    /**
     * 服务发现名称, 与注册中心类型关联
     *
     * @return 名称
     */
    String name();
}
