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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;
import com.huaweicloud.sermant.utils.RocketmqWrapperUtils;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * interceptor单元测试基类
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class BasePushConsumerInterceptorTest {
    protected DefaultMQPushConsumer pushConsumer;

    protected DefaultMqPushConsumerWrapper pushConsumerWrapper;

    @Before
    public void before() {
        pushConsumer = new DefaultMQPushConsumer("test-group");
        pushConsumerWrapper = createPushConsumerWrapper();
        MockedStatic<RocketmqWrapperUtils> wrapperUtilsMockedStatic = Mockito
                .mockStatic(RocketmqWrapperUtils.class);
        wrapperUtilsMockedStatic.when(() -> RocketmqWrapperUtils.wrapPushConsumer(pushConsumer))
                .thenReturn(Optional.of(pushConsumerWrapper));
    }

    private ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClientIP("test-ip");
        clientConfig.setInstanceName("test-consumer");
        clientConfig.setNamesrvAddr("test-add");
        return clientConfig;
    }

    protected DefaultMqPushConsumerWrapper createPushConsumerWrapper() {
        DefaultMQPushConsumerImpl pushConsumerImpl = pushConsumer.getDefaultMQPushConsumerImpl();
        MQClientInstance clientInstance = Mockito.mock(MQClientInstance.class);
        Mockito.when(clientInstance.getClientConfig()).thenReturn(createClientConfig());
        return new DefaultMqPushConsumerWrapper(pushConsumer, pushConsumerImpl, clientInstance);
    }
}
