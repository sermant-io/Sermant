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

package com.huawei.dubbo.registry.service;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

/**
 * The interface configuration service, the reflection call class method is used in the code to be compatible with both
 * Alibaba and Apache Dubbo
 *
 * @author provenceee
 * @since 2021-12-15
 */
public interface ApplicationConfigService extends PluginService {
    /**
     * Obtain the dubbo service name
     *
     * @param obj Enhanced classes
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    void getName(Object obj);
}
