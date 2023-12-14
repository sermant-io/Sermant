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

package com.huaweicloud.sermant.kafka.controller;

import com.huaweicloud.sermant.kafka.cache.KafkaConsumerCache;
import com.huaweicloud.sermant.kafka.cache.KafkaConsumerWrapper;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Set;

/**
 * KafkaConsumer消费控制器
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerController {
    private KafkaConsumerController() {
    }

    /**
     * 关闭消费
     *
     * @param kafkaConsumerWrapper 消费者包装实例
     * @param topics 消费主题
     */
    public static void disableConsumption(KafkaConsumerWrapper kafkaConsumerWrapper, Set<String> topics) {
    }

    /**
     * 开启消费
     *
     * @param kafkaConsumerWrapper 消费者包装实例
     * @param topics 消费主题
     */
    public static void enableConsumption(KafkaConsumerWrapper kafkaConsumerWrapper, Set<String> topics) {
    }

    /**
     * 更新消费者缓存
     *
     * @param kafkaConsumer 消费者实例
     */
    public static void updateConsumerCache(KafkaConsumer<?, ?> kafkaConsumer) {
        KafkaConsumerCache.INSTANCE.updateCache(kafkaConsumer);
    }

    /**
     * 获取消费者缓存
     *
     * @return 消费者缓存
     */
    public static Set<KafkaConsumerWrapper> getConsumerCache() {
        return KafkaConsumerCache.INSTANCE.getCache();
    }
}
