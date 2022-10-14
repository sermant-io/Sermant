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

package com.huaweicloud.sermant.kafka.interceptor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.message.common.config.DenyConsumeConfig;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.logging.Logger;

/**
 * kafka consumer的构造方法拦截器，在kafka构造方法执行之后立即关闭KafkaConsumer<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-09
 */
public class KafkaConsumerConstructorInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        final DenyConsumeConfig pluginConfig = PluginConfigManager.getPluginConfig(DenyConsumeConfig.class);
        if (pluginConfig.isEnableKafkaDeny()) {
            Object object = context.getObject();
            if (object instanceof KafkaConsumer) {
                KafkaConsumer<?, ?> consumer = (KafkaConsumer<?, ?>) object;

                // 方法执行结束后，直接关闭consumer
                consumer.close();
                LOGGER.info("kafka consumer has been closed by sermant");
            }
        }
        return context;
    }
}
