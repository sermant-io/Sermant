/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.backend.kafka;

import com.huawei.sermant.backend.common.conf.KafkaConf;
import com.huawei.sermant.backend.common.exception.KafkaTopicException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaConsumerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerManager.class);

    private KafkaConsumer<String, String> consumer;

    private static KafkaConsumerManager instance;

    private KafkaConsumerManager(KafkaConf conf) {
        setConsumerConf(conf);
    }

    /**
     * 获取kafka消费者管理类实例
     *
     * @param conf kafka配置
     * @return kafka消费者管理实例
     */
    public static synchronized KafkaConsumerManager getInstance(KafkaConf conf) {
        if (instance == null) {
            instance = new KafkaConsumerManager(conf);
        }
        return instance;
    }

    /**
     * 配置kafkaConf
     *
     * @param conf kafka配置信息
     * @throws KafkaTopicException kafka主题异常
     */
    private void setConsumerConf(KafkaConf conf) throws KafkaTopicException {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getBootStrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, conf.getKafkaKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, conf.getKafkaValueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, conf.getKafkaGroupId());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, conf.getKafkaEnableAutoCommit());
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, conf.getKafkaAutoCommitIntervalMs());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, conf.getKafkaAutoOffsetReset());
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, conf.getKafkaSessionTimeoutMs());
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, conf.getKafkaFetchMinBytes());
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, conf.getKafkaFetchMaxWaitMs());
        consumer = new KafkaConsumer<String, String>(properties);
    }

    /**
     * 获取消费者
     *
     * @return 消费者
     */
    public KafkaConsumer<String, String> getConsumer() {
        return consumer;
    }
}
