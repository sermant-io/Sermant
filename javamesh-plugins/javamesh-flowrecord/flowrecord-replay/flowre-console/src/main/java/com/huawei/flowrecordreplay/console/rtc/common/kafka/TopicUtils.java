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

package com.huawei.flowrecordreplay.console.rtc.common.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.ConfigResource.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * topic工具类：主要对topic进行增删改查
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@Component
public class TopicUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicUtils.class);
    /**
     * 自动注入kafka客户端
     */
    @Autowired
    private AdminClient adminClient;
    /**
     * 主题
     */
    @Value("${topics:topic-heartbeat,topic-metric}")
    private String topics;
    /**
     * 分区数
     */
    @Value("${partitions.init:3}")
    private int partitions;
    /**
     * 副本数
     */
    @Value("${replication-factor.init:1}")
    private short replicationfactors;

    /**
     * 创建主题：同时指定分区数和副本数
     *
     * @param topic             主题
     * @param numpartitions     分区个数
     * @param replicationFactor 副本个数
     * @return CreateTopicsResult
     */
    public CreateTopicsResult createTopic(String topic, int numpartitions, short replicationFactor) {
        CreateTopicsResult createTopics;
        try {
            NewTopic newTopic = new NewTopic(topic, numpartitions, replicationFactor);
            createTopics = adminClient.createTopics(Collections.singleton(newTopic));
            return createTopics;
        } finally {
            adminClient.close();
        }
    }

    /**
     * 删除主题：可以删除多个
     *
     * @param topicArray 主题集
     * @return DeleteTopicsResult
     */
    public DeleteTopicsResult deleteTopics(String[] topicArray) {
        if (topicArray.length <= 0) {
            throw new IllegalArgumentException("topics is empty");
        }
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, topicArray);
        return adminClient.deleteTopics(list);
    }

    /**
     * 显示所有主题
     *
     * @return ListTopicsResult
     */
    public ListTopicsResult listTopics() {
        return adminClient.listTopics();
    }

    /**
     * 查看主题描述
     *
     * @param topic 主题
     * @return Map map集合
     */
    public Map<String, TopicDescription> describeTopic(String topic) {
        Map<String, TopicDescription> map = null;
        try {
            DescribeTopicsResult describeTopics = adminClient.describeTopics(Collections.singleton(topic));
            KafkaFuture<Map<String, TopicDescription>> all = describeTopics.all();
            map = all.get();
            return map;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("describeTopic is error：", e);
        }
        return map;
    }

    /**
     * 查看多个主题描述
     *
     * @param topicArray 主题集
     * @return Map map集合
     */
    public Map<String, TopicDescription> describeTopic(String[] topicArray) {
        Map<String, TopicDescription> map = null;
        try {
            ArrayList<String> list = new ArrayList<>();
            Collections.addAll(list, topicArray);
            DescribeTopicsResult describeTopics = adminClient.describeTopics(list);
            KafkaFuture<Map<String, TopicDescription>> all = describeTopics.all();
            map = all.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("describeTopic is error：", e);
        }
        return map;
    }

    /**
     * 修改主题配置
     *
     * @param topic  主题
     * @param config 配置对象
     */
    public void alterConfig(String topic, Config config) {
        ConfigResource configResource = new ConfigResource(Type.TOPIC, topic);
        Map<ConfigResource, Config> configs = new HashMap<>();
        configs.put(configResource, config);
        adminClient.alterConfigs(configs);
    }

    /**
     * 判断是否是空：topic数为0
     *
     * @return boolean
     */
    public boolean isEmpty() {
        boolean isEmpty = false;
        try {
            ListTopicsResult listTopics = listTopics();
            isEmpty = listTopics.names().get().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("isEmpty is error：", e);
        }
        return isEmpty;
    }

    /**
     * 初始化topic
     *
     * @return boolean
     */
    public boolean initTopics() {
        boolean isSuccessful = false;
        try {
            String[] topicsStr = topics.split(",");
            List<NewTopic> topicList = new ArrayList<>();
            for (String topic : topicsStr) {
                topicList.add(new NewTopic(topic, partitions, replicationfactors));
            }
            adminClient.createTopics(topicList);
            isSuccessful = true;
        } catch (KafkaException e) {
            LOGGER.error("initTopics is error：", e);
        }
        return isSuccessful;
    }
}
