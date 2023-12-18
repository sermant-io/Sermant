/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.kafka.cache;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.NetworkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka实例包装类
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerWrapper {
    private KafkaConsumer<?, ?> kafkaConsumer;

    /**
     * 宿主应用自身订阅的Topic
     */
    private Set<String> originalTopics;

    /**
     * 是否使用assign方法指定订阅
     */
    private boolean isAssign;

    /**
     * 使用assign方法指定的Topic和分区
     */
    private Collection<TopicPartition> originalPartitions;

    /**
     * 禁止消费的动态配置是否已更新
     */
    private AtomicBoolean isConfigChanged;

    /**
     * 当前消费者的服务所在可用区
     */
    private String zone;

    /**
     * 当前消费者的服务所在可用区命名空间
     */
    private String project;

    /**
     * 当前消费者的服务所在的环境
     */
    private String environment;

    /**
     * 当前消费者的服务所在的应用
     */
    private String application;

    /**
     * 当前消费者的所在服务的名称
     */
    private String service;

    /**
     * 当前消费者的所在服务的IP
     */
    private String serverAddress;

    /**
     * 构造方法
     *
     * @param consumer 消费者原始实例
     */
    public KafkaConsumerWrapper(KafkaConsumer<?, ?> consumer) {
        ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        this.kafkaConsumer = consumer;
        this.zone = serviceMeta.getZone();
        this.project = serviceMeta.getProject();
        this.environment = serviceMeta.getEnvironment();
        this.application = serviceMeta.getApplication();
        this.service = serviceMeta.getService();
        this.serverAddress = NetworkUtils.getMachineIp();
        this.originalTopics = new HashSet<>();
        this.originalPartitions = new HashSet<>();
        this.isAssign = false;
        this.isConfigChanged = new AtomicBoolean(false);
    }

    public KafkaConsumer<?, ?> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public void setKafkaConsumer(KafkaConsumer<?, ?> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    public Set<String> getOriginalTopics() {
        return originalTopics;
    }

    public void setOriginalTopics(Set<String> originalTopics) {
        this.originalTopics = originalTopics;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public boolean isAssign() {
        return isAssign;
    }

    public void setAssign(boolean assign) {
        this.isAssign = assign;
    }

    public Collection<TopicPartition> getOriginalPartitions() {
        return originalPartitions;
    }

    public void setOriginalPartitions(Collection<TopicPartition> originalPartitions) {
        this.originalPartitions = originalPartitions;
    }

    public AtomicBoolean getIsConfigChanged() {
        return isConfigChanged;
    }

    public void setConfigChanged(AtomicBoolean configChanged) {
        this.isConfigChanged = configChanged;
    }
}
