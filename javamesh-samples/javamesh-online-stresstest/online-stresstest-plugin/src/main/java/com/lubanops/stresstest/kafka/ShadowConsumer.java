/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.kafka;

import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.apache.kafka.common.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 自定义consumer，可用于消费正常topic和影子topic
 *
 * @param <K> Key
 * @param <V> Value
 */
public class ShadowConsumer<K, V> implements Consumer<K, V> {
    private static final int SIZE = 100;

    private KafkaConsumer<K, V>  originalConsumer;

    private KafkaConsumer<K, V> testConsumer;

    private boolean useOriginal;

    public ShadowConsumer(KafkaConsumer<K, V>  originalConsumer) {
        this.originalConsumer = originalConsumer;
        this.testConsumer = ConsumerBuilder.initTestConsumer(originalConsumer);
        this.useOriginal = true;
    }

    @Override
    public Set<TopicPartition> assignment() {
        Set<TopicPartition> set = new HashSet<>();
        set.addAll(originalConsumer.assignment());
        set.addAll(testConsumer.assignment());
        return set;
    }

    @Override
    public Set<String> subscription() {
        Set<String> set = new HashSet<>();
        set.addAll(originalConsumer.subscription());
        set.addAll(testConsumer.subscription());
        return set;
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
        originalConsumer.subscribe(pattern, callback);
        Set<String> topics = originalConsumer.subscription();
        testConsumer.subscribe(getShadowTopics(topics), callback);
    }

    @Override
    public void subscribe(Pattern pattern) {
        subscribe(pattern, new NoOpConsumerRebalanceListener());
    }

    @Override
    public void subscribe(Collection topics, ConsumerRebalanceListener callback) {
        originalConsumer.subscribe(topics, callback);
        testConsumer.subscribe(getShadowTopics(topics), callback);
    }

    @Override
    public void subscribe(Collection topics) {
        subscribe(topics, new NoOpConsumerRebalanceListener());
    }

    @Override
    public void unsubscribe() {
        originalConsumer.unsubscribe();
        testConsumer.unsubscribe();
    }

    @Override
    public ConsumerRecords poll(long timeout) {
        return poll(Duration.ofMillis(timeout));
    }

    @Override
    public ConsumerRecords poll(Duration timeout) {
        if (useOriginal) {
            return originalPoll(timeout);
        }
        return testPoll(timeout);
    }

    private ConsumerRecords originalPoll(Duration timeout) {
        Tester.setTest(false);
        ConsumerRecords records = originalConsumer.poll(timeout);
        if (records.count() < SIZE) {
            useOriginal = false;
        }
        return records;
    }

    private ConsumerRecords testPoll(Duration timeout) {
        Tester.setTest(true);
        ConsumerRecords records = testConsumer.poll(timeout);
        useOriginal = true;
        return records;
    }

    @Override
    public void commitSync() {
        chooseConsumer().commitSync();
    }

    @Override
    public void commitSync(Duration timeout) {
        chooseConsumer().commitSync(timeout);
    }

    @Override
    public void commitSync(Map offsets, Duration timeout) {
        chooseConsumer().commitSync(offsets, timeout);
    }

    @Override
    public void commitSync(Map offsets) {
        chooseConsumer().commitSync(offsets);
    }

    @Override
    public void commitAsync() {
        commitAsync(null);
    }

    @Override
    public void commitAsync(OffsetCommitCallback callback) {
        if (Tester.isTest()) {
            testConsumer.commitAsync(wrapShadowCallback(callback));
        } else {
            originalConsumer.commitAsync(callback);
        }
    }

    @Override
    public void commitAsync(Map offsets, OffsetCommitCallback callback) {
        if (Tester.isTest()) {
            testConsumer.commitAsync(offsets, wrapShadowCallback(callback));
        } else {
            originalConsumer.commitAsync(offsets, callback);
        }
    }

    @Override
    public void seek(TopicPartition partition, long offset) {
        if (Tester.isTestTopic(partition.topic())) {
            testConsumer.seek(partition, offset);
        } else {
            originalConsumer.seek(partition, offset);
        }
    }

    @Override
    public void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {
        if (Tester.isTestTopic(partition.topic())) {
            testConsumer.seek(partition, offsetAndMetadata);
        } else {
            originalConsumer.seek(partition, offsetAndMetadata);
        }
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> collection) {
        TopicCollection topicCollection = new TopicCollection(collection);
        originalConsumer.seekToEnd(topicCollection.original);
        testConsumer.seekToEnd(topicCollection.test);
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> collection) {
        TopicCollection topicCollection = new TopicCollection(collection);
        originalConsumer.seekToBeginning(topicCollection.original);
        testConsumer.seekToBeginning(topicCollection.test);
    }

    @Override
    public long position(TopicPartition partition) {
        return Tester.isTestTopic(partition.topic()) ? testConsumer.position(partition) : originalConsumer.position(partition);
    }

    @Override
    public long position(TopicPartition partition, Duration timeout) {
        return Tester.isTestTopic(partition.topic()) ? testConsumer.position(partition, timeout) : originalConsumer.position(partition, timeout);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition) {
        return Tester.isTestTopic(partition.topic()) ? testConsumer.committed(partition) : originalConsumer.committed(partition);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition, Duration timeout) {
        return Tester.isTestTopic(partition.topic()) ? testConsumer.committed(partition, timeout) : originalConsumer.committed(partition, timeout);
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set, Duration timeout) {
        TopicCollection topicCollection = new TopicCollection(set);
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        map.putAll(originalConsumer.committed((Set<TopicPartition>) topicCollection.original, timeout));
        map.putAll(testConsumer.committed((Set<TopicPartition>) topicCollection.test, timeout));
        return map;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> set) {
        TopicCollection topicCollection = new TopicCollection(set);
        Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
        map.putAll(originalConsumer.committed((Set<TopicPartition>) topicCollection.original));
        map.putAll(testConsumer.committed((Set<TopicPartition>) topicCollection.test));
        return map;
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return chooseConsumer().metrics();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return Tester.isTestTopic(topic) ? testConsumer.partitionsFor(topic) : originalConsumer.partitionsFor(topic);
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic, Duration timeout) {
        return Tester.isTestTopic(topic) ? testConsumer.partitionsFor(topic, timeout) : originalConsumer.partitionsFor(topic, timeout);
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        Map<String, List<PartitionInfo>> result = new HashMap<>();
        result.putAll(originalConsumer.listTopics());
        result.putAll(testConsumer.listTopics());
        return result;
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics(Duration timeout) {
        Map<String, List<PartitionInfo>> result = new HashMap<>();
        result.putAll(originalConsumer.listTopics(timeout));
        result.putAll(testConsumer.listTopics(timeout));
        return result;
    }

    @Override
    public Set<TopicPartition> paused() {
        Set<TopicPartition> set = originalConsumer.paused();
        Set<TopicPartition> testSet = testConsumer.paused();
        Set<TopicPartition> result = new HashSet<>();
        result.addAll(set);
        result.addAll(testSet);
        return result;
    }

    @Override
    public ConsumerGroupMetadata groupMetadata() {
        return chooseConsumer().groupMetadata();
    }

    @Override
    public void close() {
        originalConsumer.close();
        testConsumer.close();
    }

    @Override
    public void close(long timeout, TimeUnit unit) {
        originalConsumer.close(timeout, unit);
        testConsumer.close(timeout, unit);
    }

    @Override
    public void close(Duration timeout) {
        originalConsumer.close(timeout);
        testConsumer.close(timeout);
    }

    @Override
    public void wakeup() {
        originalConsumer.wakeup();
        testConsumer.wakeup();
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection collection, Duration timeout) {
        Map<TopicPartition, Long> map = new HashMap<>();
        TopicCollection topicCollection = new TopicCollection(collection);
        map.putAll(originalConsumer.endOffsets(topicCollection.original, timeout));
        map.putAll(testConsumer.endOffsets(topicCollection.test, timeout));
        return map;
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection collection) {
        Map<TopicPartition, Long> map = new HashMap<>();
        TopicCollection topicCollection = new TopicCollection(collection);
        map.putAll(originalConsumer.endOffsets(topicCollection.original));
        map.putAll(testConsumer.endOffsets(topicCollection.test));
        return map;
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection collection, Duration timeout) {
        Map<TopicPartition, Long> map = new HashMap<>();
        TopicCollection topicCollection = new TopicCollection(collection);
        map.putAll(originalConsumer.beginningOffsets(topicCollection.original, timeout));
        map.putAll(testConsumer.beginningOffsets(topicCollection.test, timeout));
        return map;
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection collection) {
        Map<TopicPartition, Long> map = new HashMap<>();
        TopicCollection topicCollection = new TopicCollection(collection);
        map.putAll(originalConsumer.beginningOffsets(topicCollection.original));
        map.putAll(testConsumer.beginningOffsets(topicCollection.test));
        return map;
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch, Duration timeout) {
        TopicMap<Long> topicMap = new TopicMap<>(timestampsToSearch);
        Map<TopicPartition, OffsetAndTimestamp> results = new HashMap<>();
        results.putAll(originalConsumer.offsetsForTimes(topicMap.original, timeout));
        results.putAll(testConsumer.offsetsForTimes(topicMap.test, timeout));
        return results;
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
        TopicMap<Long> topicMap = new TopicMap<>(timestampsToSearch);
        Map<TopicPartition, OffsetAndTimestamp> results = new HashMap<>();
        results.putAll(originalConsumer.offsetsForTimes(topicMap.original));
        results.putAll(testConsumer.offsetsForTimes(topicMap.test));
        return results;
    }

    @Override
    public void resume(Collection<TopicPartition> collection) {
        TopicCollection topicCollection = new TopicCollection(collection);
        originalConsumer.resume(topicCollection.original);
        testConsumer.resume(topicCollection.test);
    }

    @Override
    public void pause(Collection<TopicPartition> collection) {
        TopicCollection topicCollection = new TopicCollection(collection);
        originalConsumer.pause(topicCollection.original);
        testConsumer.pause(topicCollection.test);
    }

    @Override
    public void assign(Collection collection) {
        TopicCollection topicCollection = new TopicCollection(collection);
        originalConsumer.assign(topicCollection.original);
        testConsumer.assign(topicCollection.test);
    }

    private KafkaConsumer chooseConsumer() {
        return Tester.isTest()? testConsumer:originalConsumer;
    }

    private OffsetCommitCallback wrapShadowCallback(OffsetCommitCallback callback) {
        if (callback == null) {
            return null;
        }
        return (offsets, exception) -> {
            Tester.setTest(true);
            try {
                callback.onComplete(offsets, exception);
            } finally {
                Tester.setTest(false);
            }
        };
    }

    private static Set<String> getShadowTopics(Collection<String> topics) {
        Set<String> results = new HashSet<>();
        if (topics != null) {
            String prefix = ConfigFactory.getConfig().getTestTopicPrefix();
            for (String topic : topics) {
                if (!Tester.isTestTopic(topic)) {
                    results.add(prefix + topic);
                }
            }
        }
        return results;
    }

    private static class TopicCollection {
        private Set<TopicPartition> original;
        private Set<TopicPartition> test;

        TopicCollection(Collection<TopicPartition> collection) {
            original = new HashSet<>();
            test = new HashSet<>();
            for (TopicPartition item: collection) {
                if (Tester.isTestTopic(item.topic())) {
                    test.add(item);
                } else {
                    original.add(item);
                }
            }
        }
    }

    private static class TopicMap<V> {
        private Map<TopicPartition, V> original;
        private Map<TopicPartition, V> test;

        TopicMap(Map<TopicPartition, V> map) {
            original = new HashMap<>();
            test = new HashMap<>();
            for (Map.Entry<TopicPartition, V> item: map.entrySet()) {
                if (Tester.isTestTopic(item.getKey().topic())) {
                    test.put(item.getKey(), item.getValue());
                } else {
                    original.put(item.getKey(), item.getValue());
                }
            }
        }
    }
}
