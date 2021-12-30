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

package com.huawei.gray.feign.service;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.plugin.service.PluginService;

import java.lang.reflect.Method;

/**
 * 获取服务下游列表服务
 *
 * @author fuziye
 * @since 2021-12-29
 */
public interface ServiceInstanceListSupplierService extends PluginService {
    /**
     * 拦截点前执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult 执行结果承载类
     * @throws Exception 增强时可能出现的异常
     */
    void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception;

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     * @throws Exception 增强时可能出现的异常
     */
    void after(Object obj, Method method, Object[] arguments, Object result) throws Exception;
}
