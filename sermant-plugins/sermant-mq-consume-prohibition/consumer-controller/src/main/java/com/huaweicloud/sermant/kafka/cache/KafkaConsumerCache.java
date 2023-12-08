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

package com.huaweicloud.sermant.kafka.cache;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * KafkaConsumer缓存
 *
 * @author lilai
 * @since 2023-12-05
 */
public enum KafkaConsumerCache {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 消费者缓存
     */
    private final Set<KafkaConsumerWrapper> kafkaConsumerCache = new CopyOnWriteArraySet<>();

    KafkaConsumerCache() {
        init();
    }

    private void init() {

    }

    /**
     * 获取消费者缓存
     *
     * @return 消费者缓存
     */
    public Set<KafkaConsumerWrapper> getCache() {
        return kafkaConsumerCache;
    }

    /**
     * 更新Kafka消费者缓存列表
     *
     * @param kafkaConsumer 消费者实例
     */
    public void updateCache(KafkaConsumer<?, ?> kafkaConsumer) {
        kafkaConsumerCache.add(convert(kafkaConsumer));
    }

    /**
     * 消费者实例转换
     *
     * @param kafkaConsumer 原始消费者实例
     * @return 消费者包装实例
     */
    private KafkaConsumerWrapper convert(KafkaConsumer<?, ?> kafkaConsumer) {
        return new KafkaConsumerWrapper();
    }
}
