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

package com.huawei.dubbo.register.service;

import com.huawei.sermant.core.plugin.service.PluginService;

/**
 * 接口配置服务，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021/12/15
 */
public interface RegistryConfigService extends PluginService {
    /**
     * 增加注册中心配置以注册到sc
     *
     * @param obj 增强的类
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    void addRegistryConfig(Object obj);
}