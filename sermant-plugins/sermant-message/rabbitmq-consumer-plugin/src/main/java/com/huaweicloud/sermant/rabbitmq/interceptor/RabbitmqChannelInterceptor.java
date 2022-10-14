/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.rabbitmq.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.message.common.config.DenyConsumeConfig;
import com.huaweicloud.sermant.rabbitmq.declarer.RabbitmqChannelDeclarer;

/**
 * 用于对rabbit mq的一个拦截操作<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-11
 */
public class RabbitmqChannelInterceptor extends AbstractInterceptor {
    private static final String MOCK_CONSUMER_TAG = "";
    private static final String MOCK_VOID = null;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        final DenyConsumeConfig pluginConfig = PluginConfigManager.getPluginConfig(DenyConsumeConfig.class);
        if (pluginConfig.isUseRabbitmq()) {
            skip(context);
        }
        return context;
    }

    private void skip(ExecuteContext context) {
        String methodName = context.getMethod().getName();
        if (RabbitmqChannelDeclarer.BASIC_CONSUME.equals(methodName)) {
            context.skip(MOCK_CONSUMER_TAG);
        } else if (RabbitmqChannelDeclarer.BASIC_ACK.equals(methodName)) {
            context.skip(MOCK_VOID);
        }
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
