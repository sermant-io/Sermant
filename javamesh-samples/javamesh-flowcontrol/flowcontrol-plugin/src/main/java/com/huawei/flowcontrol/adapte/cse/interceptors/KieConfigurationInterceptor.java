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
 * KieConfiguration拦截
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class KieConfigurationInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (method.getName().equals(CseConstants.PROJECT_METHOD)) {
            CseServiceMeta.getInstance().setProject((String) arguments[0]);
        } else if (method.getName().equals(CseConstants.ENVIRONMENT_METHOD)) {
            CseServiceMeta.getInstance().setEnvironment((String) arguments[0]);
        } else if (method.getName().equals(CseConstants.CUSTOM_LABEL_METHOD)) {
            CseServiceMeta.getInstance().setCustomLabel((String) arguments[0]);
        }  else if (method.getName().equals(CseConstants.APP_NAME_METHOD)) {
            CseServiceMeta.getInstance().setApp((String) arguments[0]);
        } else {
            CseServiceMeta.getInstance().setCustomLabelValue((String) arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return null;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
