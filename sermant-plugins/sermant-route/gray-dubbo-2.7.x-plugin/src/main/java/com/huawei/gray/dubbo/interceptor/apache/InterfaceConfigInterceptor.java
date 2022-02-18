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

import com.huawei.gray.dubbo.service.InterfaceConfigService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强AbstractInterfaceConfig类的getApplication方法，用来获取应用名
 *
 * @author provenceee
 * @since 2021年11月8日
 */
public class InterfaceConfigInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private InterfaceConfigService interfaceConfigService;

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        interfaceConfigService = ServiceManager.getService(InterfaceConfigService.class);
    }

    /**
     * Dubbo启动时，获取并缓存应用名
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     * @return 返回值
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        interfaceConfigService.getName(result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "AbstractInterfaceConfig is error!", throwable);
    }
}