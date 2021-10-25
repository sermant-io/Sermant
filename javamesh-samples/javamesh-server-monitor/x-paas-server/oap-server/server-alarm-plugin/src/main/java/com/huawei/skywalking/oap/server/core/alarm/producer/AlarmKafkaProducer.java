/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.core.alarm.producer;

import com.huawei.skywalking.oap.server.core.alarm.config.KafkaConst;

import com.google.common.collect.Lists;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.skywalking.oap.server.core.alarm.provider.AlarmSettings;

/**
 * kafka生产者
 *
 * @author hudeyu
 * @since 2021-07-27
 */
@Slf4j
public class AlarmKafkaProducer {
    private static AlarmKafkaProducer alarmKafkaProducer;

    private AlarmKafkaProducer() {
    }

    /**
     * 生成kafka生产者对象
     *
     * @param config AlarmSettings 对象
     * @return KafkaProducer kafka生产者
     */
    public KafkaProducer<String, String> getKafkaProducer(AlarmSettings config) {
        Properties props = new Properties();
        props.put(KafkaConst.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(KafkaConst.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        props.put(KafkaConst.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());
        props.put(KafkaConst.BATCH_SIZE_CONFIG, config.getBatchSize());
        props.put(KafkaConst.RETRIES_CONFIG, config.getRetries());
        props.put(KafkaConst.ACKS_CONFIG, config.getAcks());
        props.put(KafkaConst.LINGER_MS_CONFIG, config.getLingerMs());
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);
        initTopic(config);
        return kafkaProducer;
    }

    private void initTopic(AlarmSettings config) {
        String topic = config.getTopic();
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        AdminClient adminClient = AdminClient.create(properties);
        TopicDescription topicDescription = null;
        try {
            topicDescription = adminClient.describeTopics(Lists.newArrayList(topic)).values().get(topic).get();
            log.debug("Topic {} is exist.", topic);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Topic {} isn't exist.", topic);
        }
        if (topicDescription == null) {
            try {
                int partitions = config.getPartitions();
                short replicationFactor = (short) config.getReplicationFactor();
                adminClient.createTopics(
                        Lists.newArrayList(new NewTopic(topic, partitions, replicationFactor))).all().get();
                log.info("Topic {} Creates successfully with parameter: partitions-{}, replicationFactor-{}",
                        topic, partitions, replicationFactor);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Topic {} create fail,", e);
            }
        }
    }

    /**
     * 获取AlarmKafkaProducer对象
     *
     * @return AlarmKafkaProducer对象
     */
    public static AlarmKafkaProducer getAlarmKafkaProducer() {
        if (alarmKafkaProducer == null) {
            synchronized (AlarmKafkaProducer.class) {
                if (alarmKafkaProducer == null) {
                    alarmKafkaProducer = new AlarmKafkaProducer();
                }
            }
        }
        return alarmKafkaProducer;
    }
}
