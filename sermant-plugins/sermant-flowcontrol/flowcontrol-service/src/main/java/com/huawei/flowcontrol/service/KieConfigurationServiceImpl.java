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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.util.SpiLoadUtil.SpiWeight;

import java.lang.reflect.Method;

/**
 * 拦截KieConfiguration获取配置信息
 *
 * @author zhouss
 * @since 2021-12-27
 */
public class KieConfigurationServiceImpl extends KieConfigurationService {
    /**
     * 拦截点前执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult 执行结果承载类
     */
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
}
