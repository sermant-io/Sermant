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

package com.huawei.dubbo.register.interceptor;

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import org.apache.dubbo.common.URL;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强InterfaceCompatibleRegistryProtocol类的getServiceDiscoveryInvoker方法
 *
 * @author provenceee
 * @date 2022/1/26
 */
public class RegistryProtocolInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 这个方法是为了让2.7.9不去加载sc ServiceDiscovery
     *
     * @param obj 增强实例
     * @param method 原方法
     * @param arguments 原方法参数
     * @param beforeResult 前置结果
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (arguments != null && arguments.length > 2 && arguments[2] instanceof URL) {
            if (Constant.SC_REGISTRY_PROTOCOL.equals(((URL) arguments[2]).getProtocol())) {
                // sc协议的注册，直接return，这样就可以不去加载sc ServiceDiscovery，即屏蔽sc应用级注册
                beforeResult.setResult(null);
            }
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "InterfaceCompatibleRegistryProtocol is error!", throwable);
    }
}