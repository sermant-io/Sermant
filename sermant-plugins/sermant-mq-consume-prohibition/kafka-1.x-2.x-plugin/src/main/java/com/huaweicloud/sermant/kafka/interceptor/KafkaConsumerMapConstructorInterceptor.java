/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.kafka.extension.KafkaConsumerHandler;
import com.huaweicloud.sermant.kafka.utils.MarkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.logging.Logger;

/**
 * KafkaConsumer Map构造方法的拦截器
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Map, Deserializer, Deserializer)}
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerMapConstructorInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private KafkaConsumerHandler handler;

    /**
     * 带有KafkaConsumerHandler的构造方法
     *
     * @param handler 构造方法拦截点处理器
     */
    public KafkaConsumerMapConstructorInterceptor(KafkaConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * 无参数构造方法
     */
    public KafkaConsumerMapConstructorInterceptor() {
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        // 高版本Properties会调用Map方式，避免重复进入
        if (MarkUtils.getMark() != null) {
            return context;
        }
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        // 高版本Properties会调用Map方式，避免重复进入
        if (MarkUtils.getMark() != null) {
            return context;
        }
        if (handler != null) {
            handler.doAfter(context);
        }

        cacheKafkaConsumer(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        // 高版本Properties会调用Map方式，避免重复进入
        if (MarkUtils.getMark() != null) {
            return context;
        }
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }

    /**
     * 缓存消费者实例
     *
     * @param context 拦截点执行上下文
     */
    private void cacheKafkaConsumer(ExecuteContext context) {
        Object kafkaConsumerObject = context.getObject();
        if (kafkaConsumerObject instanceof KafkaConsumer) {
            KafkaConsumer<?, ?> consumer = (KafkaConsumer<?, ?>) kafkaConsumerObject;
            KafkaConsumerController.addKafkaConsumerCache(consumer);
            LOGGER.info("KafkaConsumer has been cached by Sermant.");
        }
    }
}
