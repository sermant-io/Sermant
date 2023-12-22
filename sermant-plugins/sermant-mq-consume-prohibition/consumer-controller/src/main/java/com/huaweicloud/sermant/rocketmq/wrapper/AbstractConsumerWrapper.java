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

package com.huaweicloud.sermant.rocketmq.wrapper;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消费者包装抽象类
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public abstract class AbstractConsumerWrapper {
    /**
     * rocketmq消费者的私有属性，用于加入或退出消费者组
     */
    protected final MQClientInstance clientFactory;

    /**
     * 消费者是否已经禁止消费
     */
    protected AtomicBoolean prohibition = new AtomicBoolean();

    /**
     * nameserver地址
     */
    protected String nameServerAddress;

    /**
     * rocketmq消费者组
     */
    protected String consumerGroup;

    /**
     * 消费者实例ip
     */
    protected String clientIp;

    /**
     * 消费者实例名称
     */
    protected String instanceName;

    /**
     * 当前消费者的服务所在可用区
     */
    protected String zone;

    /**
     * 当前消费者的服务所在可用区命名空间
     */
    protected String project;

    /**
     * 当前消费者的服务所在环境
     */
    protected String environment;

    /**
     * 当前消费者的服务所在应用
     */
    protected String application;

    /**
     * 当前消费者所在服务的名称
     */
    protected String service;

    /**
     * 消费者已订阅消费主题
     */
    protected Set<String> subscribedTopics = new HashSet<>();

    /**
     * 有参构造方法
     *
     * @param clientFactory 消费者内部工厂类
     */
    protected AbstractConsumerWrapper(MQClientInstance clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * 初始化消费者实例的信息
     */
    protected abstract void initClientInfo();

    public boolean isProhibition() {
        return prohibition.get();
    }

    /**
     * 设置是否已经禁止消费
     *
     * @param prohibition 是否禁止消费
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

    /**
     * 添加订阅的topic
     *
     * @param topic 订阅的主题
     */
    public void addSubscribedTopics(String topic) {
        this.subscribedTopics.add(topic);
    }

    /**
     * 移除取消订阅的topic
     *
     * @param topic 取消订阅的主题
     */
    public void removeSubscribedTopics(String topic) {
        this.subscribedTopics.remove(topic);
    }
}