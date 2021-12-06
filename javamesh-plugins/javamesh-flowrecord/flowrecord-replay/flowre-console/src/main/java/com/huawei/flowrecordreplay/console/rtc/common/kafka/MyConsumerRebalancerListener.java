/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import com.huawei.flowrecordreplay.console.rtc.common.redis.RedisUtil;
import com.huawei.flowrecordreplay.console.rtc.common.utils.CommonTools;

import io.lettuce.core.RedisException;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 该类用于kafka消费者在订阅主题的时候传入的重平衡处理类
 * <p>
 * 实现了ConsumerRebalanceListener接口，该接口定义了两个方法
 * onPartitionsRevoked和onPartitionsAssigned，前者表示任务被取消的时候的监听动作，
 * 后者表示接收到新任务时的监听动作
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component
@Primary
public class MyConsumerRebalancerListener implements ConsumerRebalanceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumerRebalancerListener.class);
    /**
     * 自动注入kafka消费者对象
     */
    @Autowired
    private KafkaConsumer<String, String> consumer;

    /**
     * 自动注入redis工具类的对象
     */
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 重平衡时设置的安全偏移量，以防丢数据，即便可能是重复消费
     */
    @Value("${kafka.consumer.rebalance.safeOffset:1000}")
    private long safeOffset;

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
                redisUtil.set(partition.topic() + ":" + partition.partition(), String.valueOf(offset));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("save partition after kafka rebalanced：{} offset：{}", partition, offset);
                }
            } catch (RedisException e) {
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
        CommonTools.consumePartitionOffsetAssigned(redisUtil, consumer, partitions, safeOffset);
    }
}