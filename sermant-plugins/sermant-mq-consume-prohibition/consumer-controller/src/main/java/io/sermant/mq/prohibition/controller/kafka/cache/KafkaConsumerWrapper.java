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

package io.sermant.mq.prohibition.controller.kafka.cache;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.utils.NetworkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka instance wrapper
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerWrapper {
    private KafkaConsumer<?, ?> kafkaConsumer;

    /**
     * The topic to which the host application subscribes
     */
    private Set<String> originalTopics;

    /**
     * Whether to use the assign method to specify the subscription
     */
    private boolean isAssign;

    /**
     * Use the assign method to specify the topic and partition
     */
    private Collection<TopicPartition> originalPartitions;

    /**
     * Whether the dynamic configuration for which consumption is prohibited has been updated
     */
    private AtomicBoolean isConfigChanged;

    /**
     * The zone in which the consumer's service is located
     */
    private String zone;

    /**
     * The namespace of the AZ where the consumer's service is located
     */
    private String project;

    /**
     * The environment in which the current consumer's services are located
     */
    private String environment;

    /**
     * The application in which the service of the current consumer resides
     */
    private String application;

    /**
     * The name of the service on which the current consumer is located
     */
    private String service;

    /**
     * The IP address of the service where the current consumer is located
     */
    private String serverAddress;

    /**
     * Constructor
     *
     * @param consumer Consumer origin instance
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
