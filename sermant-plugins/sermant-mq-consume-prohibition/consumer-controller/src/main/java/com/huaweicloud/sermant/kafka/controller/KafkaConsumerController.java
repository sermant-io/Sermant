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
 * KafkaConsumer消费控制器
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerController {
    private KafkaConsumerController() {
    }

    /**
     * 执行禁止消费
     *
     * @param kafkaConsumerWrapper 消费者包装实例
     * @param prohibitionTopics 禁止消费的主题
     */
    public static void disableConsumption(KafkaConsumerWrapper kafkaConsumerWrapper, Set<String> prohibitionTopics) {
        Set<String> originalTopics = kafkaConsumerWrapper.getOriginalTopics();

        // 未订阅任何Topic，无需操作
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
     * 新增消费者缓存
     *
     * @param kafkaConsumer 消费者实例
     */
    public static void addKafkaConsumerCache(KafkaConsumer<?, ?> kafkaConsumer) {
        KafkaConsumerCache.INSTANCE.addKafkaConsumer(kafkaConsumer);
    }

    /**
     * 获取消费者缓存
     *
     * @return 消费者缓存
     */
    public static Map<Integer, KafkaConsumerWrapper> getKafkaConsumerCache() {
        return KafkaConsumerCache.INSTANCE.getCache();
    }

    /**
     * 移除消费者缓存
     *
     * @param kafkaConsumer 消费者实例
     */
    public static void removeKafkaConsumeCache(KafkaConsumer<?, ?> kafkaConsumer) {
        KafkaConsumerCache.INSTANCE.getCache().remove(kafkaConsumer.hashCode());
    }
}
