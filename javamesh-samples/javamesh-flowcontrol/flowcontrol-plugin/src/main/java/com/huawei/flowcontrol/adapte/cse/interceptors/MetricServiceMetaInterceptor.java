/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.interceptors;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.flowcontrol.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;

import java.lang.reflect.Method;

/**
 * 拦截获取当前服务的版本
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class MetricServiceMetaInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {

    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        if (method.getName().equals(CseConstants.SERVICE_NAME_METHOD)) {
            CseServiceMeta.getInstance().setService(String.valueOf(result));
        } else if (method.getName().equals(CseConstants.SERVICE_VERSION_METHOD)) {
            CseServiceMeta.getInstance().setVersion(String.valueOf(result));
        } else {
            return result;
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
