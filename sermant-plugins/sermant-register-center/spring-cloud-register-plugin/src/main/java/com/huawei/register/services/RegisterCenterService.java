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

package com.huawei.register.services;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.plugin.service.PluginService;

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
     * @param registration 注册信息
     * @param result 前置返回
     */
    void register(Object registration, BeforeResult result);

    /**
     * 替换服务列表
     *
     * @param target 被增强对象
     * @param beforeResult 前置返回结果
     */
    void replaceServerList(Object target, BeforeResult beforeResult);

    /**
     * 针对DiscoverClient#getInstances, 会携带serviceId
     *
     * @param serviceId 服务名
     * @param beforeResult 前置返回结果
     */
    void replaceServerList(String serviceId, BeforeResult beforeResult);
}
