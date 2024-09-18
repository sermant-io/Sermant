/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.mq.grayscale.rocketmq;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.consumer.RebalancePushImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * test abstract
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqTestAbstract {
    private static MockedStatic<ConfigManager> configManagerMockedStatic;

    @BeforeClass
    public static void setUp() {
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        ServiceMeta serviceMeta = new ServiceMeta();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("x_lane_tag", "gray");
        serviceMeta.setParameters(parameters);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class)).thenReturn(serviceMeta);
        createConfig();
    }

    protected static void createConfig() {
        GrayConfigContextUtils.createMqGrayConfig(null, DynamicConfigEventType.CREATE);
    }

    @AfterClass
    public static void tearDown() {
        configManagerMockedStatic.close();
    }

    protected RebalanceImpl getPushConsumerRebalanced(DefaultMQPushConsumerImpl pushConsumer) {
        RebalanceImpl rebalanced = new RebalancePushImpl(pushConsumer);
        setRebalancedImplInfo(rebalanced);
        return rebalanced;
    }

    protected void setRebalancedImplInfo(RebalanceImpl rebalanced) {
        rebalanced.setConsumerGroup("consumerGroup");
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNamesrvAddr("127.0.0.1:9876");
        MQClientInstance instance = new MQClientInstance(clientConfig, 0, "1");
        rebalanced.setmQClientFactory(instance);
    }
}
