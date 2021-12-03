/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.adapte.cse.interceptors;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
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
        } else if (method.getName().equals(CseConstants.APP_NAME_METHOD)) {
            CseServiceMeta.getInstance().setApp((String) arguments[0]);
        } else if (method.getName().equals(CseConstants.SERVICE_NAME_METHOD)) {
            CseServiceMeta.getInstance().setServiceName((String) arguments[0]);
        } else {
            CseServiceMeta.getInstance().setCustomLabelValue((String) arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
