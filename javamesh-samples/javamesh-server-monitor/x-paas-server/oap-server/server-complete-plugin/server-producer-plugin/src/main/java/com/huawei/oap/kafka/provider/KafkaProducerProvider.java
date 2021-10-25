/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.kafka.provider;

import com.google.common.collect.Lists;
import com.huawei.oap.kafka.module.KafkaProducerConfig;
import com.huawei.oap.kafka.module.KafkaProducerModule;
import com.huawei.oap.kafka.service.IKafkaService;
import com.huawei.oap.kafka.service.KafkaServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author hefan
 * @since 2021-06-21
 */
@Slf4j
public class KafkaProducerProvider extends ModuleProvider {
    private KafkaProducerConfig config;

    public KafkaProducerProvider() {
        config = new KafkaProducerConfig();
    }

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return KafkaProducerModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return config;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException, ModuleStartException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getBatchSize());
        props.put(ProducerConfig.RETRIES_CONFIG, config.getRetries());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getClientId());
        props.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        props.put(ProducerConfig.LINGER_MS_CONFIG, config.getLingerMs());
        KafkaProducer<String, Bytes> producer = new KafkaProducer<>(props);

        KafkaServiceImpl kafkaService = new KafkaServiceImpl(producer, config.getTopic());
        this.registerServiceImplementation(IKafkaService.class, kafkaService);

        initTopic();
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
    }

    @Override
    public String[] requiredModules() {
        return new String[0];
    }

    private void initTopic() {
        String topic = config.getTopic();
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        AdminClient adminClient = AdminClient.create(props);
        TopicDescription topicDescription = null;
        try {
            topicDescription = adminClient.describeTopics(Lists.newArrayList(topic)).values().get(topic).get();
            log.debug("Topic {} is exist.", topic);
        } catch (InterruptedException | ExecutionException e) {
            log.info("Topic {} isn't exist.", topic);
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
                log.error("", e);
            }
        }
    }
}
