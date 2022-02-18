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

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @author provenceee
 * @since 2021年6月28日
 */
public class ClusterInterceptor implements StaticMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String GRAY_VERSION_KEY = "gray.version";

    private static final String GRAY_LDC_KEY = "ldc";

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (arguments.length > 1 && arguments[1] instanceof Map<?, ?>) {
            Map<String, String> map = new HashMap<String, String>((Map<String, String>) arguments[1]);
            map.remove(GRAY_VERSION_KEY);
            map.remove(GRAY_LDC_KEY);
            arguments[1] = map;
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "ClusterUtils is error!", throwable);
    }
}