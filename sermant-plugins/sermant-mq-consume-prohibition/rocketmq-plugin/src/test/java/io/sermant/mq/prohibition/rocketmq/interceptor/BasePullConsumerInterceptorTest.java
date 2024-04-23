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

package io.sermant.mq.prohibition.rocketmq.interceptor;

import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import io.sermant.mq.prohibition.controller.utils.RocketMqWrapperUtils;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * interceptor unit test base class
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class BasePullConsumerInterceptorTest {
    protected DefaultLitePullConsumer pullConsumer;

    protected DefaultLitePullConsumerWrapper pullConsumerWrapper;

    @Before
    public void before() throws MQClientException {
        pullConsumer = new DefaultLitePullConsumer("test-group");
        pullConsumerWrapper = createPullConsumerWrapper();
        MockedStatic<RocketMqWrapperUtils> wrapperUtilsMockedStatic = Mockito
                .mockStatic(RocketMqWrapperUtils.class);
        wrapperUtilsMockedStatic.when(() -> RocketMqWrapperUtils.wrapPullConsumer(pullConsumer))
                .thenReturn(Optional.of(pullConsumerWrapper));
    }

    private ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClientIP("test-ip");
        clientConfig.setInstanceName("test-consumer");
        clientConfig.setNamesrvAddr("test-add");
        return clientConfig;
    }

    protected DefaultLitePullConsumerWrapper createPullConsumerWrapper() {
        DefaultLitePullConsumerImpl pullConsumerImpl = Mockito.mock(DefaultLitePullConsumerImpl.class);
        RebalanceImpl rebalanceImpl = Mockito.mock(RebalanceImpl.class);
        MQClientInstance clientInstance = Mockito.mock(MQClientInstance.class);
        Mockito.when(clientInstance.getClientConfig()).thenReturn(createClientConfig());
        return new DefaultLitePullConsumerWrapper(pullConsumer, pullConsumerImpl,
                rebalanceImpl, clientInstance);
    }
}
