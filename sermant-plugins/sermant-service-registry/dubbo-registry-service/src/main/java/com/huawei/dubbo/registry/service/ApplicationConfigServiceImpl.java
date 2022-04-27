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

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.utils.StringUtils;

/**
 * 应用配置服务，代码中使用反射调用类方法是为了同时兼容alibaba和apache dubbo
 *
 * @author provenceee
 * @since 2021-12-31
 */
public class ApplicationConfigServiceImpl implements ApplicationConfigService {
    /**
     * 设置注册时的服务名
     *
     * @param obj 增强的类
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Override
    public void getName(Object obj) {
        String name = ReflectUtils.getName(obj);
        if (StringUtils.isBlank(name)) {
            return;
        }
        DubboCache.INSTANCE.setServiceName(name);
    }
}