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

package com.huawei.recordconsole.consumer;

import com.huawei.recordconsole.config.CommonConfig;
import com.huawei.recordconsole.strategy.InterfaceTopicHandleStrategy;
import com.huawei.recordconsole.strategy.TopicHandleStrategyFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * kafka 录制kafka consumer
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-02-19
 *
 */

@Slf4j
@Component
public class RecordConsoleConsumer {
    private static final long SLEEP_MS = 500L;
    /**
     * 自动注入kafka消费者对象
     */
    @Autowired
    private KafkaConsumer<String, String> consumer;
//    /**
//     * 重平衡处理类
//     */
//    @Autowired
//    private ConsumerRebalanceListener consumerRebalanceListener;

    /**
     * 消息的处理工厂
     */
    @Autowired
    private TopicHandleStrategyFactory topicHandleStrategyFactory;

    /**
     * 注入线程池
     */
    @Autowired
    private ExecutorService pool;

    /**
     * 重平衡时设置的安全偏移量，以防丢数据，即便可能是重复消费
     */
    @Value("${kafka.consumer.rebalance.safeOffset:1000}")
    private long safeOffset;

    /**
     * 是否循环拉取的标志
     */
    private volatile boolean isRunning = true;

    /**
     * 主题集
     */
    @Value("${topics:request,response}")
    private String topics;

    /**
     * kafka消费者一次拉取时的超时时间
     */
    @Value("${kafka.consumer.poll.timeout:1000}")
    private long timeout;

    /**
     * kafka消费者的启动方法，该方法在容器启动的时候调用，并不断地循环处理数据，在方法内部发生异常时停止循环，在停止之前同步提交
     */
    public void start() {
        final Thread mainThread = Thread.currentThread();
        setExit(mainThread);
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, topics.split(","));
        consumer.subscribe(list);

        // 在kafka消费者在启动的时候，指定partition消费，也要执行consumer.poll(0)，目的是为了获取offset;
        firstPoll();

        // 正式拉取并处理消息
        pollAndHandleMessage();
    }

    private void pollAndHandleMessage() {
        try {
            // 实时拉取
            while (isRunning) {
                try {
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(timeout);
                    if (consumerRecords.isEmpty()) {
                        continue;
                    }
                    Set<TopicPartition> partitions = consumerRecords.partitions();
                    for (TopicPartition topicPartition : partitions) {
                        List<ConsumerRecord<String, String>> records = consumerRecords.records(topicPartition);
                        String topic = topicPartition.topic();

                        // 逻辑处理
                        logicProcess(topic, records, topicPartition);

                        // 异步提交
                        commitAsynchronized(records, topicPartition);
                    }
                } catch (WakeupException e) {
                    log.info("WakeupException：", e);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    log.info("IllegalArgumentException | IllegalStateException", e);
                } catch (Exception exception) {
                    throw exception;
                }
            }
        } catch (Exception exception) {
            // 处理异常
            log.info("error", exception);
        } finally {
            consumer.commitSync();
            log.info("commitSync");
            close();
        }
    }

    private void logicProcess(String topic, List<ConsumerRecord<String, String>> records, TopicPartition topicPartition)
            throws ExecutionException, InterruptedException, IOException {
        InterfaceTopicHandleStrategy handler = topicHandleStrategyFactory.getTopicHandleStrategy(topic);
        handler.handleRecordByTopic(records);
    }

    /**
     * 优雅退出：
     * <p>
     * 退出循环需要通过另一个线程调用consumer.wakeup()方法
     * 调用consumer.wakeup()可以退出poll(),并抛出WakeupException异常
     * 我们不需要处理 WakeupException,因为它只是用于跳出循环的一种方式
     * consumer.wakeup()是消费者唯一一个可以从其他线程里安全调用的方法
     * 如果循环运行在主线程里，可以在 ShutdownHook里调用该方法
     *
     * @param mainThread 主线程
     */
    private void setExit(Thread mainThread) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("consumer Starting exit...");
            consumer.wakeup();
            try {
                // 主线程继续执行，以便可以关闭consumer，提交偏移量
                mainThread.join();
            } catch (InterruptedException e) {
                log.info(e.toString(), e);
            }
        }));
    }

    private void firstPoll() {
        Set<TopicPartition> assignmentPartitions = new HashSet<>();
        while (assignmentPartitions.isEmpty()) {
            try {
                consumer.poll(Duration.ZERO);
                assignmentPartitions = consumer.assignment();
            } catch (IllegalArgumentException | IllegalStateException e) {
                break;
            }
        }
    }

    /**
     * 关闭消费者资源
     */
    public void close() {
        isRunning = false;
        if (consumer != null) {
            consumer.close();
        }
    }

    /**
     * consumer异步提交方法，以分区为提交单位
     *
     * @param recordsInPartition 分区中的记录数
     * @param topicPartition     主题分区对象
     */
    private void commitAsynchronized(List<ConsumerRecord<String, String>> recordsInPartition,
                                     TopicPartition topicPartition) {
        long lastConsumedOffset = recordsInPartition.get(recordsInPartition.size() - 1).offset();
        consumer.commitAsync(
                Collections.singletonMap(
                        topicPartition,
                        new OffsetAndMetadata(lastConsumedOffset + 1)
                ),
                (offsets, exception) -> {
                    int retries = CommonConfig.RECORD_COMMIT_RETRIES_TIME;
                    if (exception == null) {
                        // 异步提交成功
                        log.info("commitAsynchronized：{}", offsets.entrySet().toArray()[0]);
                    } else {
                        // 异步提交失败
                        log.info(exception.toString(), exception);
                        while (retries > 0) {
                            retries--;

                            // 提交重试
                            consumer.commitSync();
                            try {
                                Thread.sleep(SLEEP_MS);
                            } catch (InterruptedException e) {
                                log.info("{}/{}commitSync failed", retries, e);
                            }
                        }
                    }
                }
        );
    }
}
