/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.prohibition.controller.kafka;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.mq.prohibition.controller.kafka.cache.KafkaConsumerWrapper;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * KafkaConsumerController unit test
 *
 * @author lilai
 * @since 2023-12-23
 */
public class KafkaConsumerControllerTest {
    private MockedStatic<ConfigManager> configManagerMockedStatic;

    @Before
    public void setUp() {
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(new ServiceMeta());
    }

    @After
    public void tearDown() {
        configManagerMockedStatic.close();
    }

    /**
     * Test the situation where originalTopics is empty
     */
    @Test
    public void testDisableConsumptionWithNoTopic() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(mockConsumer);
        HashSet<String> originalTopics = new HashSet<>();
        kafkaConsumerWrapper.setOriginalTopics(originalTopics);
        Set<String> prohibitionTopics = new HashSet<>();
        KafkaConsumerController.disableConsumption(kafkaConsumerWrapper, prohibitionTopics);
        Mockito.verify(mockConsumer, Mockito.times(0)).subscribe(
                (Collection<String>) Mockito.any());
        Mockito.verify(mockConsumer, Mockito.times(0)).assign(Mockito.any());
    }

    /**
     * Test the situation where the subscribe method and promotionTopics are empty
     */
    @Test
    public void testDisableConsumptionWithNoProhibitionTopics() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(mockConsumer);
        HashSet<String> originalTopics = new HashSet<>();
        originalTopics.add("testTopic-1");
        kafkaConsumerWrapper.setOriginalTopics(originalTopics);
        kafkaConsumerWrapper.setAssign(false);
        Set<String> prohibitionTopics = new HashSet<>();
        KafkaConsumerController.disableConsumption(kafkaConsumerWrapper, prohibitionTopics);
        Mockito.verify(mockConsumer, Mockito.times(1)).subscribe(
                Collections.singletonList("testTopic-1"));
    }

    /**
     * Test the subtraction of productionTopics from originalTopics in the subscribe method
     */
    @Test
    public void testDisableConsumptionWithSubtractTopics() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(mockConsumer);
        HashSet<String> originalTopics = new HashSet<>();
        originalTopics.add("testTopic-1");
        originalTopics.add("testTopic-2");
        kafkaConsumerWrapper.setOriginalTopics(originalTopics);
        kafkaConsumerWrapper.setAssign(false);
        Set<String> prohibitionTopics = new HashSet<>();
        prohibitionTopics.add("testTopic-2");
        prohibitionTopics.add("testTopic-3");
        KafkaConsumerController.disableConsumption(kafkaConsumerWrapper, prohibitionTopics);
        Mockito.verify(mockConsumer, Mockito.times(1)).subscribe(Collections.singletonList("testTopic-1"));
    }

    /**
     * Test the absence of intersection between originalTopics and promotionTopics
     */
    @Test
    public void testDisableConsumptionWithoutSameTopics() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(mockConsumer);
        HashSet<String> originalTopics = new HashSet<>();
        originalTopics.add("testTopic-1");
        originalTopics.add("testTopic-2");
        kafkaConsumerWrapper.setOriginalTopics(originalTopics);
        kafkaConsumerWrapper.setAssign(false);
        Set<String> prohibitionTopics = new HashSet<>();
        prohibitionTopics.add("testTopic-3");
        KafkaConsumerController.disableConsumption(kafkaConsumerWrapper, prohibitionTopics);
        Mockito.verify(mockConsumer, Mockito.times(1)).subscribe(Arrays.asList("testTopic-1", "testTopic-2"));
    }

    /**
     * Test the assignment method by subtracting promotionTopics from originalTopics
     */
    @Test
    public void testDisableConsumptionByAssign() {
        KafkaConsumer<?, ?> mockConsumer = Mockito.mock(KafkaConsumer.class);
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(mockConsumer);
        HashSet<String> originalTopics = new HashSet<>();
        originalTopics.add("testTopic-1");
        originalTopics.add("testTopic-2");
        HashSet<TopicPartition> originalPartitions = new HashSet<>();
        TopicPartition topicPartition1 = new TopicPartition("testTopic-1", 0);
        TopicPartition topicPartition2 = new TopicPartition("testTopic-1", 1);
        TopicPartition topicPartition3 = new TopicPartition("testTopic-2", 0);
        originalPartitions.add(topicPartition1);
        originalPartitions.add(topicPartition2);
        originalPartitions.add(topicPartition3);
        kafkaConsumerWrapper.setOriginalTopics(originalTopics);
        kafkaConsumerWrapper.setOriginalPartitions(originalPartitions);
        kafkaConsumerWrapper.setAssign(true);
        Set<String> prohibitionTopics = new HashSet<>();
        prohibitionTopics.add("testTopic-2");
        prohibitionTopics.add("testTopic-3");
        KafkaConsumerController.disableConsumption(kafkaConsumerWrapper, prohibitionTopics);
        originalPartitions.remove(topicPartition3);
        Mockito.verify(mockConsumer, Mockito.times(1)).assign(originalPartitions);
    }
}
