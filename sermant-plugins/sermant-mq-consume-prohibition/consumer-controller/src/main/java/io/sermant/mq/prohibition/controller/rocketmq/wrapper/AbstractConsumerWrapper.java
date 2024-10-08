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

package io.sermant.mq.prohibition.controller.rocketmq.wrapper;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class of consumer packaging
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public abstract class AbstractConsumerWrapper {
    /**
     * A private property of a RocketMQ consumer that is used to join or leave a consumer group
     */
    protected final MQClientInstance clientFactory;

    /**
     * Whether the consumer has banned consumption
     */
    protected AtomicBoolean prohibition = new AtomicBoolean();

    /**
     * nameserver address
     */
    protected String nameServerAddress;

    /**
     * RocketMQ Consumer Group
     */
    protected String consumerGroup;

    /**
     * Consumer instance IP
     */
    protected String clientIp;

    /**
     * The name of the consumer instance
     */
    protected String instanceName;

    /**
     * The zone in which the consumer's service is located
     */
    protected String zone;

    /**
     * The namespace of the AZ where the consumer's service is located
     */
    protected String project;

    /**
     * The environment in which the current consumer's service is located
     */
    protected String environment;

    /**
     * The application in which the service of the current consumer resides
     */
    protected String application;

    /**
     * The name of the service that the current consumer is using
     */
    protected String service;

    /**
     * The consumer has subscribed to a consumption topic
     */
    protected Set<String> subscribedTopics = new HashSet<>();

    /**
     * parameter construction method
     *
     * @param clientFactory Consumer internal factory class
     */
    protected AbstractConsumerWrapper(MQClientInstance clientFactory) {
        this.clientFactory = clientFactory;
    }

    public boolean isProhibition() {
        return prohibition.get();
    }

    /**
     * Set whether the consumption has been prohibited
     *
     * @param prohibition Whether consumption is prohibited
     */
    public void setProhibition(boolean prohibition) {
        this.prohibition.set(prohibition);
    }

    public MQClientInstance getClientFactory() {
        return clientFactory;
    }

    public String getNameServerAddress() {
        return nameServerAddress;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getInstanceName() {
        return instanceName;
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

    public Set<String> getSubscribedTopics() {
        return subscribedTopics;
    }

    public void setSubscribedTopics(Set<String> subscribedTopics) {
        this.subscribedTopics = subscribedTopics;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /**
     * Add a topic to which you subscribe
     *
     * @param topic Subscribed topic
     */
    public void addSubscribedTopics(String topic) {
        this.subscribedTopics.add(topic);
    }

    /**
     * Remove the unsubscribed topic
     *
     * @param topic Unsubscribed topic
     */
    public void removeSubscribedTopics(String topic) {
        this.subscribedTopics.remove(topic);
    }
}
