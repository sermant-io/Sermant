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

package com.huawei.gray.dubbo.interceptor.apache;

import com.huawei.gray.dubbo.service.RegistryDirectoryService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强RegistryDirectory类的doList方法，筛选灰度应用的地址
 *
 * @author provenceee
 * @since 2021年6月28日
 */
public class RegistryDirectoryInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private RegistryDirectoryService registryDirectoryService;

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        registryDirectoryService = ServiceManager.getService(RegistryDirectoryService.class);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return registryDirectoryService.selectInvokers(arguments, result);
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "RegistryDirectory is error!", throwable);
    }
}