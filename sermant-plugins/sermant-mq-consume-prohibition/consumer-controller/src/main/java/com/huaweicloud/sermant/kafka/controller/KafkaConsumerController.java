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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The consumption controller of a Kafka consumer
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerController {
    private KafkaConsumerController() {
    }

    /**
     * Enforcement of prohibited consumption
     *
     * @param kafkaConsumerWrapper instance of consumer packaging
     * @param prohibitionTopics Topics that are prohibited for consumption
     */
    public static void disableConsumption(KafkaConsumerWrapper kafkaConsumerWrapper, Set<String> prohibitionTopics) {
        Set<String> originalTopics = kafkaConsumerWrapper.getOriginalTopics();

        // Not subscribed to any Topic, so no action is required
        if (originalTopics.size() == 0) {
            return;
        }
        Collection<TopicPartition> originalPartitions = kafkaConsumerWrapper.getOriginalPartitions();
        KafkaConsumer<?, ?> kafkaConsumer = kafkaConsumerWrapper.getKafkaConsumer();
        Collection<String> subtractTopics = CollectionUtils.subtract(originalTopics, prohibitionTopics);
        if (kafkaConsumerWrapper.isAssign()) {
            kafkaConsumer.assign(originalPartitions.stream().filter(obj -> subtractTopics.contains(obj.topic()))
                    .collect(Collectors.toSet()));
            return;
        }
        kafkaConsumer.subscribe(subtractTopics);
    }

    /**
     * Added consumer cache
     *
     * @param kafkaConsumer Consumer instance
     */
    public static void addKafkaConsumerCache(KafkaConsumer<?, ?> kafkaConsumer) {
        KafkaConsumerCache.INSTANCE.addKafkaConsumer(kafkaConsumer);
    }

    /**
     * Get the consumer cache
     *
     * @return consumer cache
     */
    public static Map<Integer, KafkaConsumerWrapper> getKafkaConsumerCache() {
        return KafkaConsumerCache.INSTANCE.getCache();
    }

    /**
     * Remove the consumer cache
     *
     * @param kafkaConsumer consumer cache
     */
    public static void removeKafkaConsumeCache(KafkaConsumer<?, ?> kafkaConsumer) {
        KafkaConsumerCache.INSTANCE.getCache().remove(kafkaConsumer.hashCode());
    }
}
