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

package com.huawei.sermant.plugin.connection.pool.collect.interceptor;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.plugin.connection.pool.collect.service.DruidMonitorService;

import java.lang.reflect.Method;

/**
 * Druid Monitor 移除数据源拦截器
 */
public class RemoveDataSourceInterceptor implements StaticMethodInterceptor {

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        if (arguments != null && arguments.length > 0) {
            Object dataSource = arguments[0];
            if (dataSource instanceof DruidDataSource) {
                DruidMonitorService.getInstance().removeDataSource((DruidDataSource) dataSource);
            }
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
