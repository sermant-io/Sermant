/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.kafka.mock;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 用于kafka禁写的mock的kafka consumer<br>
 * 非close方法都执行该mock的consumer，而close方法不能增强
 *
 * @author yuzl 俞真龙
 * @param <K> key
 * @param <V> value
 * @since 2022-10-09
 */
public class MockKafkaConsumer<K, V> implements Consumer<K, V> {
    @Override
    public Set<TopicPartition> assignment() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> subscription() {
        return Collections.emptySet();
    }

    @Override
    public void assign(Collection<TopicPartition> partitions) {
    }

    @Override
    public void subscribe(Collection<String> topics) {
    }

    @Override
    public void subscribe(Collection<String> topics, ConsumerRebalanceListener callback) {
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
    }

    @Override
    public void subscribe(Pattern pattern) {
    }

    @Override
    public void unsubscribe() {
    }

    @Override
    public ConsumerRecords<K, V> poll(long timeout) {
        return new ConsumerRecords<>(Collections.emptyMap());
    }

    @Override
    public ConsumerRecords<K, V> poll(Duration timeout) {
        return new ConsumerRecords<>(Collections.emptyMap());
    }

    @Override
    public void commitSync() {
    }

    @Override
    public void commitSync(Duration timeout) {
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets, Duration timeout) {
    }

    @Override
    public void commitAsync() {
    }

    @Override
    public void commitAsync(OffsetCommitCallback callback) {
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
    }

    @Override
    public void seek(TopicPartition partition, long offset) {
    }

    @Override
    public void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> partitions) {
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> partitions) {
    }

    @Override
    public long position(TopicPartition partition) {
        return 0;
    }

    @Override
    public long position(TopicPartition partition, Duration timeout) {
        return 0;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition) {
        return committed(partition, Duration.ZERO);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition, Duration timeout) {
        return new OffsetAndMetadata(0);
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions, Duration timeout) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return Collections.emptyMap();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return Collections.emptyList();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic, Duration timeout) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics(Duration timeout) {
        return Collections.emptyMap();
    }

    @Override
    public Set<TopicPartition> paused() {
        return Collections.emptySet();
    }

    @Override
    public void pause(Collection<TopicPartition> partitions) {
    }

    @Override
    public void resume(Collection<TopicPartition> partitions) {
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch,
        Duration timeout) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions, Duration timeout) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions) {
        return Collections.emptyMap();
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions, Duration timeout) {
        return Collections.emptyMap();
    }

    @Override
    public ConsumerGroupMetadata groupMetadata() {
        return new ConsumerGroupMetadata("");
    }

    @Override
    public void enforceRebalance() {
    }

    @Override
    public void close() {
    }

    @Override
    public void close(long timeout, TimeUnit unit) {
    }

    @Override
    public void close(Duration timeout) {
    }

    @Override
    public void wakeup() {
    }
}
