/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.analyzer.agent.kafka;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import com.huawei.oap.redis.service.IRedisService;
import io.netty.util.concurrent.DefaultThreadFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.module.KafkaFetcherConfig;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.provider.handler.KafkaHandler;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Configuring and initializing a KafkaConsumer client as a dispatcher to delivery Kafka Message to registered handler by topic.
 */
@Slf4j
public class KafkaFetcherHandlerRegister implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaFetcherHandlerRegister.class);

    /**
     * offset重试提交次数
     */
    private static final int OFFSET_COMMIT_RETRY_TIMES = 3;

    /**
     * 重试间隔时间，单位ms
     */
    private static final long SLEEP_MS = 500L;

    private ImmutableMap.Builder<String, KafkaHandler> builder = ImmutableMap.builder();
    private ImmutableMap<String, KafkaHandler> handlerMap;

    private List<TopicPartition> topicPartitions = Lists.newArrayList();
    private KafkaConsumer<String, Bytes> consumer = null;
    private final KafkaFetcherConfig config;
    private final boolean isSharding;

    private IRedisService redisService;

    public KafkaFetcherHandlerRegister(KafkaFetcherConfig config) throws ModuleStartException {
        this.config = config;
        Properties properties = new Properties(config.getKafkaConsumerConfig());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());

        AdminClient adminClient = AdminClient.create(properties);
        Set<String> missedTopics = adminClient.describeTopics(Lists.newArrayList(
                config.getTopicNameOfManagements(),
                config.getTopicNameOfMetrics(),
                config.getTopicNameOfProfiling(),
                config.getTopicNameOfTracingSegments(),
                config.getTopicNameOfMeters(),
                // update Huawei APM #46 change log:添加连接池指标数据主题名称
                config.getTopicNameOfDataSource()
        ))
                .values()
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        entry.getValue().get();
                        return null;
                    } catch (InterruptedException | ExecutionException e) {
                    }
                    return entry.getKey();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!missedTopics.isEmpty()) {
            log.info("Topics" + missedTopics.toString() + " not exist.");
            List<NewTopic> newTopicList = missedTopics.stream()
                    .map(topic -> new NewTopic(
                            topic,
                            config.getPartitions(),
                            (short) config.getReplicationFactor()
                    )).collect(Collectors.toList());

            try {
                adminClient.createTopics(newTopicList).all().get();
            } catch (Exception e) {
                throw new ModuleStartException("Failed to create Kafka Topics" + missedTopics + ".", e);
            }
        }

        if (config.isSharding() && StringUtil.isNotEmpty(config.getConsumePartitions())) {
            isSharding = true;
        } else {
            isSharding = false;
        }
        consumer = new KafkaConsumer<>(properties, new StringDeserializer(), new BytesDeserializer());
    }

    public void register(KafkaHandler handler) {
        builder.put(handler.getTopic(), handler);
        topicPartitions.addAll(handler.getTopicPartitions());
    }

    public void start() {
        handlerMap = builder.build();
        if (isSharding) {
            consumer.assign(topicPartitions);
        } else {
            // huawei update, log: 订阅kafka时添加重平衡策略
            subscribeTopics(consumer, handlerMap.keySet(), config.getSafeOffset());
        }
        // huawei update, log: 从redis中获取分区的偏移量
        firstPoll(consumer, config.getSafeOffset());
        Executors.newSingleThreadExecutor(new DefaultThreadFactory("KafkaConsumer")).submit(this);
    }

    public void setRedisServiceImpl(IRedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // huawei update, log: 修改kafka消费者模板，提高可靠性
                ConsumerRecords<String, Bytes> consumerRecords = consumer.poll(Duration.ofMillis(500L));
                log.debug("[one poll records sum is {}]", consumerRecords.count());
                if (consumerRecords.isEmpty()) {
                    continue;
                }
                long start = System.currentTimeMillis();
                Set<TopicPartition> partitions = consumerRecords.partitions();
                for (TopicPartition topicPartition : partitions) {
                    List<ConsumerRecord<String, Bytes>> records = consumerRecords.records(topicPartition);
                    if (CollectionUtils.isEmpty(records)) {
                        continue;
                    }
                    for (ConsumerRecord<String, Bytes> record : records) {
                        try {
                            handlerMap.get(record.topic()).handle(record);
                        } catch (RuntimeException e) {
                            log.error("Handling record throws an exception ", e);
                        }
                    }

                    // 记录偏移量
                    recordOffset(records.get(records.size() - 1).offset(), topicPartition);
                    // 异步提交偏移量
                    commitOffsetAsync(consumer, records, topicPartition);
                }
                log.debug("[consume time:{}ms , records size:{}]",
                        (System.currentTimeMillis() - start), consumerRecords.count());
            } catch (WakeupException e) {
                // ignore the error
                log.error("WakeupException：", e);
            } catch (IllegalArgumentException | IllegalStateException e) {
                log.error("IllegalArgumentException | IllegalStateException", e);
            } catch (Exception e) {
                log.error("Exception：", e);
            }
        }
    }

    /**
     * 对kafka添加自定义的重平衡策略
     *
     * @param consumer kafka消费组
     * @param topics 订阅的主题
     * @param safeOffset 偏移量安全线
     */
    private void subscribeTopics(KafkaConsumer<String, Bytes> consumer,
                                       Collection<String> topics, int safeOffset) {
        consumer.subscribe(topics, new KafkaConsumerRebalanceListener(consumer, safeOffset));
        final Thread mainThread = Thread.currentThread();
        setExit(consumer, mainThread);
    }

    /**
     * 主要目的是，为了读取指定的各个分区的消费偏移量
     *
     * @param consumer   消费者对象
     * @param partitions 要获取偏移量的所有分区
     * @param safeOffset 在配置文件中可配置项，为了防止丢失数据，设置一个在现有的offset靠前消费的值
     */
    private void consumePartitionOffsetAssigned(KafkaConsumer<String, Bytes> consumer,
                                                      Collection<TopicPartition> partitions, long safeOffset) {
        for (TopicPartition partition : partitions) {
            // 接收到新任务将当前主题下的分区下的offset查出来，如果没有就从0开始，有就从下一个开始
            String offsetStr = redisService.get(partition.topic() + ":" + partition.partition());

            // 接收到新的topic，即数据库中记录不存在
            if (offsetStr == null || "".equals(offsetStr)) {
                consumer.seek(partition, 0);
            } else if (!StringUtils.isNumeric(offsetStr)) {
                LOGGER.error("the offset is not a numeric in partition {}", partition);
                consumer.seek(partition, 0);
            } else {
                long offset = Integer.parseInt(offsetStr);
                long value = offset - safeOffset;
                if (value > 0) {
                    offset = value;
                    consumer.seek(partition, offset);
                } else {
                    consumer.seek(partition, 0);
                }
            }
        }
    }

    /**
     * 在kafka消费者在启动的时候，指定partition消费，也要执行consumer.poll(0)，目的是为了获取offset;
     *
     * @param consumer   kafka消费组
     * @param safeOffset 偏移量
     */
    private void firstPoll(KafkaConsumer<String, Bytes> consumer, int safeOffset) {
        Set<TopicPartition> assignmentPartitions = new HashSet<>();
        while (assignmentPartitions.isEmpty()) {
            try {
                consumer.poll(Duration.ZERO);
                assignmentPartitions = consumer.assignment();
            } catch (IllegalArgumentException | IllegalStateException e) {
                break;
            }
        }
        consumePartitionOffsetAssigned(consumer, assignmentPartitions, safeOffset);
    }

    /**
     * 记录当前消费偏移量
     *
     * @param currentOffset  当前偏移量
     * @param topicPartition 分区
     */
    private void recordOffset(long currentOffset, TopicPartition topicPartition) {
        try {
            // 实时记录redis中各分区的消费偏移量offset
            String offset = String.valueOf(currentOffset + 1);
            redisService.set(topicPartition.topic() + ":" + topicPartition.partition(), offset);
            LOGGER.debug("[real time partition：{}, offset:{}]", topicPartition, offset);
        } catch (Exception ex) {
            LOGGER.error("[record offset to redis error!]", ex);
        }
    }

    /**
     * consumer异步提交方法，以分区为提交单位
     *
     * @param consumer           kafka消费者
     * @param recordsInPartition 分区中的记录数
     * @param topicPartition     主题分区对象
     */
    private static void commitOffsetAsync(KafkaConsumer<String, Bytes> consumer,
                                         List<ConsumerRecord<String, Bytes>> recordsInPartition,
                                         TopicPartition topicPartition) {
        long lastConsumedOffset = recordsInPartition.get(recordsInPartition.size() - 1).offset();
        consumer.commitAsync(
            Collections.singletonMap(topicPartition, new OffsetAndMetadata(lastConsumedOffset + 1)),
            (offsets, exception) -> dealException(offsets, exception, consumer));
    }

    private static void dealException(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception,
                                      KafkaConsumer<String, Bytes> consumer) {
        int retryTimes = Integer.valueOf(OFFSET_COMMIT_RETRY_TIMES);
        if (exception == null) {
            // 异步提交成功
            LOGGER.debug("[commitOffsetAsync：{}]", offsets.entrySet().toArray()[0]);
        } else {
            // 异步提交失败
            LOGGER.error("[commit offset failed! start retryTimes]", exception);
            while (retryTimes > 0) {
                retryTimes--;

                // 提交重试
                consumer.commitSync();
                try {
                    Thread.sleep(SLEEP_MS);
                } catch (InterruptedException e) {
                    LOGGER.error("[{}/{} commitSync failed!]", retryTimes, OFFSET_COMMIT_RETRY_TIMES, e);
                }
            }
        }
    }

    /**
     *
     * 优雅退出：
     * <p>
     * 退出循环需要通过另一个线程调用consumer.wakeup()方法
     * 调用consumer.wakeup()可以退出poll(),并抛出WakeupException异常
     * 我们不需要处理 WakeupException,因为它只是用于跳出循环的一种方式
     * consumer.wakeup()是消费者唯一一个可以从其他线程里安全调用的方法
     * 如果循环运行在主线程里，可以在 ShutdownHook里调用该方法
     *
     * @param consumer Kafka的consumer对象
     * @param mainThread 主线程
     */
    private static void setExit(KafkaConsumer<String, Bytes> consumer, Thread mainThread) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.debug("consumer Starting exit...");
            consumer.wakeup();
            try {
                // 主线程继续执行，以便可以关闭consumer，提交偏移量
                mainThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.toString(), e);
            }
        }));
    }

    /**
     * 关闭消费者资源
     *
     * @param consumer Kafka的consumer对象
     */
    private static void close(KafkaConsumer<String, Bytes> consumer) {
        if (consumer != null) {
            consumer.close();
        }
    }

    class KafkaConsumerRebalanceListener implements ConsumerRebalanceListener {
        private final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRebalanceListener.class);

        private KafkaConsumer<String, Bytes> consumer;
        private int safeOffset;

        KafkaConsumerRebalanceListener(KafkaConsumer<String, Bytes> consumer, int safeOffset) {
            this.consumer = consumer;
            this.safeOffset = safeOffset;
        }

        /**
         * 表示任务被取消的时候的监听动作,这里的处理逻辑是，将当前消费者处理当前分区的offset记录下来，持久化到redis中
         * <p>
         * 只会记录重平衡发生前分给改consumer的分区的消费偏移量，并且是增加和退出时触发，但是不是当前consumer执行，而是
         * 其他同组的其他consumer执行本方法。所以存在一个问题是：consumer退出时，当前consumer的负责的分区消费偏移量
         * 不会被记录到redis中，导致下次消费这块分区的consumer就会出现重复消费的现象。
         * <p>
         * 解决策略：增加实时记录消费偏移量的方法到消费完消息后。
         *
         * @param partitions 表示当前消费者处理分区集
         */
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            // 任务被取消的时候，将当前主题分区下的offset记下来，方便下一个消费者找回
            for (TopicPartition partition : partitions) {
                long offset = consumer.position(partition);
                try {
                    redisService.set(partition.topic() + ":" + partition.partition(), String.valueOf(offset));
                    LOGGER.info("save partition when rebalance：{} offset：{}", partition, offset);
                } catch (KafkaException e) {
                    LOGGER.error("throw exception when task is canceled", e);
                }
            }
        }

        /**
         * 表示接收到新任务时的监听动作,这里的处理逻辑是，将当前消费者去读取当前分区被之前的消费者消费到哪里了的offset，然后从
         * 该处继续接着消费
         *
         * @param partitions 表示当前消费者处理分区集
         */
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            consumePartitionOffsetAssigned(consumer, partitions, safeOffset);
        }
    }
}
