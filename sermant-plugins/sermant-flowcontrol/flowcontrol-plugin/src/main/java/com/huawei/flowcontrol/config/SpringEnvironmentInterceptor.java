/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.common.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.core.env.Environment;

/**
 * apache dubbo配置拦截
 *
 * @author zhouss
 * @since 2022-01-28
 */
public class SpringEnvironmentInterceptor implements Interceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        final Object result = context.getResult();
        if (!(result instanceof Environment)) {
            return context;
        }
        Environment environment = (Environment) result;
        if (pluginConfig.isUseCseRule() && pluginConfig.isBaseSdk()) {
            CseServiceMeta.getInstance().setProject(environment.getProperty(CseConstants.KEY_SPRING_KIE_PROJECT,
                    CseConstants.DEFAULT_PROJECT));
            CseServiceMeta.getInstance().setServiceName(environment.getProperty(CseConstants.KEY_SPRING_SERVICE_NAME));
            CseServiceMeta.getInstance().setEnvironment(environment.getProperty(CseConstants.KEY_SPRING_ENVIRONMENT));
            CseServiceMeta.getInstance().setApp(environment.getProperty(CseConstants.KEY_SPRING_APP_NAME));
            CseServiceMeta.getInstance().setCustomLabel(environment.getProperty(CseConstants.KEY_SPRING_CUSTOM_LABEL,
                    CseConstants.DEFAULT_CUSTOM_LABEL));
            CseServiceMeta.getInstance().setCustomLabelValue(environment.getProperty(
                    CseConstants.KEY_SPRING_CUSTOM_LABEL_VALUE, CseConstants.DEFAULT_CUSTOM_LABEL_VALUE));
            CseServiceMeta.getInstance().setVersion(environment.getProperty(CseConstants.KEY_SPRING_VERSION));
        } else {
            String serviceName = environment.getProperty(ConfigConst.SPRING_APPLICATION_NAME);
            if (StringUtils.isEmpty(serviceName)) {
                serviceName = environment.getProperty(ConfigConst.PROJECT_NAME,
                        CseConstants.DEFAULT_DUBBO_SERVICE_NAME);
            }
            CseServiceMeta.getInstance().setServiceName(serviceName);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }
}
