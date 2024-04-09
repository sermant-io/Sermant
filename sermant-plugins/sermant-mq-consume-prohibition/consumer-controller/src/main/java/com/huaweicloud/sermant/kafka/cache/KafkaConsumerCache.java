/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka consumer cache
 *
 * @author lilai
 * @since 2023-12-05
 */
public enum KafkaConsumerCache {
    /**
     * singleton
     */
    INSTANCE;

    /**
     * Consumer cache
     */
    private final Map<Integer, KafkaConsumerWrapper> kafkaConsumerCache = new ConcurrentHashMap<>();

    KafkaConsumerCache() {
    }

    /**
     * Get the consumer cache
     *
     * @return Consumer cache
     */
    public Map<Integer, KafkaConsumerWrapper> getCache() {
        return kafkaConsumerCache;
    }

    /**
     * Update the list of Kafka consumer caches
     *
     * @param kafkaConsumer consumer instance
     */
    public void addKafkaConsumer(KafkaConsumer<?, ?> kafkaConsumer) {
        kafkaConsumerCache.put(kafkaConsumer.hashCode(), convert(kafkaConsumer));
    }

    /**
     * Consumer instance transformation
     *
     * @param kafkaConsumer Original consumer instance
     * @return Examples of consumer packaging
     */
    private KafkaConsumerWrapper convert(KafkaConsumer<?, ?> kafkaConsumer) {
        return new KafkaConsumerWrapper(kafkaConsumer);
    }
}
