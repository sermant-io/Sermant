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
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.util.CollectionUtils;

import org.apache.dubbo.common.URL;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强ConfigValidationUtils类的extractRegistryType方法
 *
 * @author provenceee
 * @date 2022年1月27日
 */
public class ConfigValidationInterceptor implements StaticMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String REGISTRY_TYPE_KEY_1 = "registry-type";

    private static final String REGISTRY_TYPE_KEY_2 = "registry.type";

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (arguments[0] instanceof URL) {
            URL url = (URL) arguments[0];
            // 这个拦截点是为了把2.7.5-2.7.8的sc应用级注册给屏蔽掉
            if (Constant.SC_REGISTRY_PROTOCOL.equals(url.getProtocol())
                    && !CollectionUtils.isEmpty(url.getParameters())) {
                if (url.hasParameter(REGISTRY_TYPE_KEY_1) || url.hasParameter(REGISTRY_TYPE_KEY_2)) {
                    arguments[0] = url.removeParameters(REGISTRY_TYPE_KEY_1, REGISTRY_TYPE_KEY_2);
                }
            }
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "ConfigValidationUtils is error!", throwable);
    }
}