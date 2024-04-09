/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huawei.spring.beans.deal.interceptors;

import com.huawei.spring.beans.deal.config.SpringBeansDealConfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Automatically assemble beans to intercept specific implementations
 *
 * @author chengyouling
 * @since 2023-03-27
 */
public class SpringAutoBeanInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String SPRING_BOOT_AUTOCONFIGURE =
            "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        SpringBeansDealConfig config = PluginConfigManager.getPluginConfig(SpringBeansDealConfig.class);
        String autoConfig = config.getExcludeAutoConfigurations();
        if (!StringUtils.isEmpty(autoConfig)) {
            Object result = context.getResult();
            if (result instanceof Map) {
                removeConfigurations((Map<String, List<String>>) result, autoConfig);
            }
        }
        return context;
    }

    private void removeConfigurations(Map<String, List<String>> result, String autoConfig) {
        List<String> configurations = result.get(SPRING_BOOT_AUTOCONFIGURE);
        if (configurations == null || configurations.size() == 0) {
            return;
        }
        String[] removeBeans = autoConfig.split(",");
        List<String> newConfigurations = new ArrayList<>(configurations);
        for (String beanName : removeBeans) {
            if (newConfigurations.remove(beanName)) {
                LOGGER.info(String.format(Locale.ENGLISH, "find exclude auto bean: [%s]", beanName));
            } else {
                LOGGER.warning("not find exclude auto bean: " + beanName);
            }
        }
        result.put(SPRING_BOOT_AUTOCONFIGURE, newConfigurations);
    }
}
