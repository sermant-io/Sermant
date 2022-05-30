/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.services;

import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.List;

/**
 * 注册服务类
 *
 * @author zhouss
 * @since 2021-12-16
 */
public interface RegisterCenterService extends PluginService {
    /**
     * 拦截原spring的注册方法
     *
     * @param result 前置返回
     */
    void register(FixedResult result);

    /**
     * 停止方法
     */
    void unRegister();

    /**
     * 获取实例列表
     *
     * @param serviceId 服务名
     * @return 实例列表
     */
    List<MicroServiceInstance> getServerList(String serviceId);

    /**
     * 获取服务名列表
     *
     * @return 服务列表
     */
    List<String> getServices();

    /**
     * 获取注册中心状态
     *
     * @return UP DOWN
     */
    String getRegisterCenterStatus();

    /**
     * 获取当前实例的状态
     *
     * @return 实例状态
     */
    String getInstanceStatus();

    /**
     * 更新实例状态
     *
     * @param status 目标状态
     */
    void updateInstanceStatus(String status);
}
