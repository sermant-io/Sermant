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

package io.sermant.mq.prohibition.controller.utils;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.classloader.PluginClassLoader;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.AbstractConsumerWrapper;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultMqPushConsumerWrapper;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.Optional;

/**
 * Consumer packaging tools
 *
 * @author daizhenyu
 * @since 2023-12-14
 **/
public class RocketMqWrapperUtils {
    private RocketMqWrapperUtils() {
    }

    /**
     * packaging PullConsumer
     *
     * @param pullConsumer
     * @return PullConsumer packaging class instance
     */
    public static Optional<DefaultLitePullConsumerWrapper> wrapPullConsumer(
            DefaultLitePullConsumer pullConsumer) {
        // 获取消费者相关的defaultLitePullConsumerImpl、rebalanceImpl和mQClientFactory属性值，若获取失败说明消费者启动失败，不缓存该消费者
        Optional<DefaultLitePullConsumerImpl> pullConsumerImplOptional = getPullConsumerImpl(pullConsumer);
        if (!pullConsumerImplOptional.isPresent()) {
            return Optional.empty();
        }
        DefaultLitePullConsumerImpl pullConsumerImpl = pullConsumerImplOptional.get();

        Optional<RebalanceImpl> rebalanceImplOptional = getRebalanceImpl(pullConsumerImpl);
        if (!rebalanceImplOptional.isPresent()) {
            return Optional.empty();
        }
        RebalanceImpl rebalanceImpl = rebalanceImplOptional.get();

        Optional<MQClientInstance> clientFactoryOptional = getClientFactory(pullConsumerImpl);
        if (!clientFactoryOptional.isPresent()) {
            return Optional.empty();
        }
        MQClientInstance clientFactory = clientFactoryOptional.get();

        DefaultLitePullConsumerWrapper wrapper = new DefaultLitePullConsumerWrapper(pullConsumer, pullConsumerImpl,
                rebalanceImpl, clientFactory);
        initWrapperServiceMeta(wrapper);
        return Optional.of(wrapper);
    }

    /**
     * packaging PushConsumer
     *
     * @param pushConsumer pushConsumer instance
     * @return PushConsumer packaging class instance
     */
    public static Optional<DefaultMqPushConsumerWrapper> wrapPushConsumer(DefaultMQPushConsumer pushConsumer) {
        DefaultMQPushConsumerImpl pushConsumerImpl = pushConsumer.getDefaultMQPushConsumerImpl();
        MQClientInstance mqClientFactory = pushConsumerImpl.getmQClientFactory();

        // Obtain the defaultMQPushConsumerImpl and mQClientFactory attribute values related to the consumer. If the
        // attribute value is null, do not cache the consumer
        if (pushConsumerImpl != null && mqClientFactory != null) {
            DefaultMqPushConsumerWrapper wrapper = new DefaultMqPushConsumerWrapper(pushConsumer, pushConsumerImpl,
                    mqClientFactory);
            initWrapperServiceMeta(wrapper);
            return Optional.of(wrapper);
        }
        return Optional.empty();
    }

    /**
     * Get the assigned Message Queue attribute value of the pull consumer
     *
     * @param pullConsumerImpl Implementation class for pulling Consumer
     * @return assignedMessageQueue property value
     */
    public static Optional<AssignedMessageQueue> getAssignedMessageQueue(DefaultLitePullConsumerImpl pullConsumerImpl) {
        // Set the local class loader of the plugin class loader as the host class loader
        ((PluginClassLoader) RocketMqWrapperUtils.class.getClassLoader()).setLocalLoader(
                pullConsumerImpl.getClass().getClassLoader());

        Optional<Object> assignedMessageQueueOptional = ReflectUtils
                .getFieldValue(pullConsumerImpl, "assignedMessageQueue");
        if (assignedMessageQueueOptional.isPresent()
                && assignedMessageQueueOptional.get() instanceof AssignedMessageQueue) {
            return Optional.of((AssignedMessageQueue) assignedMessageQueueOptional.get());
        }

        // Remove the local class loader from the plugin class loader
        ((PluginClassLoader) RocketMqWrapperUtils.class.getClassLoader()).removeLocalLoader();
        return Optional.empty();
    }

    private static Optional<DefaultLitePullConsumerImpl> getPullConsumerImpl(DefaultLitePullConsumer pullConsumer) {
        Optional<Object> consumerImplOptional = ReflectUtils
                .getFieldValue(pullConsumer, "defaultLitePullConsumerImpl");
        if (consumerImplOptional.isPresent()
                && consumerImplOptional.get() instanceof DefaultLitePullConsumerImpl) {
            return Optional.of((DefaultLitePullConsumerImpl) consumerImplOptional.get());
        }
        return Optional.empty();
    }

    private static Optional<RebalanceImpl> getRebalanceImpl(DefaultLitePullConsumerImpl pullConsumerImpl) {
        Optional<Object> rebalanceImplOptional = ReflectUtils.getFieldValue(pullConsumerImpl, "rebalanceImpl");
        if (rebalanceImplOptional.isPresent()
                && rebalanceImplOptional.get() instanceof RebalanceImpl) {
            return Optional.of((RebalanceImpl) rebalanceImplOptional.get());
        }
        return Optional.empty();
    }

    private static Optional<MQClientInstance> getClientFactory(DefaultLitePullConsumerImpl pullConsumerImpl) {
        Optional<Object> clientFactoryOptional = ReflectUtils.getFieldValue(pullConsumerImpl,
                "mQClientFactory");
        if (clientFactoryOptional.isPresent()) {
            return Optional.of((MQClientInstance) clientFactoryOptional.get());
        }
        return Optional.empty();
    }

    private static void initWrapperServiceMeta(AbstractConsumerWrapper wrapper) {
        ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        wrapper.setZone(serviceMeta.getZone());
        wrapper.setProject(serviceMeta.getProject());
        wrapper.setEnvironment(serviceMeta.getEnvironment());
        wrapper.setApplication(serviceMeta.getApplication());
        wrapper.setService(serviceMeta.getService());
    }
}
