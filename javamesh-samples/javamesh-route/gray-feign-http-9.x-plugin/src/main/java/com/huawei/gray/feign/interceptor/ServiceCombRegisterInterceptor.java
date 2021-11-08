/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.gray.feign.context.CurrentInstance;
import com.huawei.gray.feign.util.RouterUtil;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import java.lang.reflect.Method;

/**
 * 拦截ServiceCombRegister创建微服务的方法
 *
 * @author lilai
 * @since 2021-11-03
 */
public class ServiceCombRegisterInterceptor implements InstanceMethodInterceptor {

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
    }

    /**
     * 拦截ServiceCombServiceRegistry.register
     *
     * @param obj       拦截对象
     * @param method    拦截方法
     * @param arguments 方法参数
     * @param result    返回结果
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (arguments[0] instanceof ServiceCombRegistration) {
            ServiceCombRegistration ServiceCombRegistration = (ServiceCombRegistration) arguments[0];
            CurrentInstance.newInstance(ServiceCombRegistration.getServiceId(), ServiceCombRegistration.getHost(),
                    ServiceCombRegistration.getPort());
            RouterUtil.init();
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
    }
}
