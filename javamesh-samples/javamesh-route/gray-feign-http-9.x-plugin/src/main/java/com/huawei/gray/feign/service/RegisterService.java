/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.feign.service;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.plugin.service.PluginService;

import java.lang.reflect.Method;

/**
 * 注册通用的service
 *
 * @author pengyuyi
 * @date 2021/11/26
 */
public interface RegisterService extends PluginService {
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
