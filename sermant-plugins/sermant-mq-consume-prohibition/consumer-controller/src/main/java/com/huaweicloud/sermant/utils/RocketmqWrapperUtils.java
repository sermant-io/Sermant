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

package com.huaweicloud.sermant.utils;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.classloader.PluginClassLoader;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.rocketmq.wrapper.AbstractConsumerWrapper;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.Optional;

/**
 * 消费者包装工具类
 *
 * @author daizhenyu
 * @since 2023-12-14
 **/
public class RocketmqWrapperUtils {
    private RocketmqWrapperUtils() {
    }

    /**
     * 包装PullConsumer
     *
     * @param pullConsumer
     * @return PullConsumer包装类实例
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
     * 包装PushConsumer
     *
     * @param pushConsumer
     * @return PushConsumer包装类实例
     */
    public static Optional<DefaultMqPushConsumerWrapper> wrapPushConsumer(DefaultMQPushConsumer pushConsumer) {
        DefaultMQPushConsumerImpl pushConsumerImpl = pushConsumer.getDefaultMQPushConsumerImpl();
        MQClientInstance mqClientFactory = pushConsumerImpl.getmQClientFactory();

        // 获取消费者相关的defaultMQPushConsumerImpl和mQClientFactory属性值，若属性值为null，不缓存该消费者;
        if (pushConsumerImpl != null && mqClientFactory != null) {
            DefaultMqPushConsumerWrapper wrapper = new DefaultMqPushConsumerWrapper(pushConsumer, pushConsumerImpl,
                    mqClientFactory);
            initWrapperServiceMeta(wrapper);
            return Optional.of(wrapper);
        }
        return Optional.empty();
    }

    /**
     * 获取pull消费者的assignedMessageQueue属性值
     *
     * @param pullConsumerImpl
     * @return assignedMessageQueue属性值
     */
    public static Optional<AssignedMessageQueue> getAssignedMessageQueue(DefaultLitePullConsumerImpl pullConsumerImpl) {
        // 设置插件类加载器的局部类加载器为宿主类加载器
        ((PluginClassLoader) RocketmqWrapperUtils.class.getClassLoader()).setLocalLoader(
                pullConsumerImpl.getClass().getClassLoader());

        Optional<Object> assignedMessageQueueOptional = ReflectUtils
                .getFieldValue(pullConsumerImpl, "assignedMessageQueue");
        if (assignedMessageQueueOptional.isPresent()
                && assignedMessageQueueOptional.get() instanceof AssignedMessageQueue) {
            return Optional.of((AssignedMessageQueue) assignedMessageQueueOptional.get());
        }

        // 移除插件类加载器的局部类加载器
        ((PluginClassLoader) RocketmqWrapperUtils.class.getClassLoader()).removeTmpLoader();
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
