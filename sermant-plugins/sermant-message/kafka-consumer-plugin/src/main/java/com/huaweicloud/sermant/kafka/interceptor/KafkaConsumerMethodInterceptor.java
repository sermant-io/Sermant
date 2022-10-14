/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huaweicloud.sermant.kafka.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.kafka.mock.MockKafkaConsumer;
import com.huaweicloud.sermant.message.common.config.DenyConsumeConfig;
import com.huaweicloud.sermant.message.common.utils.MockUtils;

import org.apache.kafka.clients.consumer.Consumer;

import java.util.Optional;

/**
 * KafkaConsumer 禁消费的一个增强拦截器，只处理非close方法<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-09
 */
public class KafkaConsumerMethodInterceptor extends AbstractInterceptor {
    private final Consumer<?, ?> mockConsumer = new MockKafkaConsumer<>();

    @Override
    public ExecuteContext before(ExecuteContext context) {
        final DenyConsumeConfig pluginConfig = PluginConfigManager.getPluginConfig(DenyConsumeConfig.class);
        if (pluginConfig.isUseKafka()) {
            Optional<Object> resultOption =
                MockUtils.invokeMethod(mockConsumer, context.getMethod(), context.getArguments());
            context.skip(resultOption.orElse(null));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
