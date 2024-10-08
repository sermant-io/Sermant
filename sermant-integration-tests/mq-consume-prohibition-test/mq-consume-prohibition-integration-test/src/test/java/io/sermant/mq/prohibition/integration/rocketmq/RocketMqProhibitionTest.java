/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.mq.prohibition.integration.rocketmq;

import io.sermant.mq.prohibition.integration.utils.DynamicConfigUtils;
import io.sermant.mq.prohibition.integration.utils.HttpRequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * 消息队列禁止消费插件rocketmq测试用例
 *
 * @author daizhenyu
 * @since 2024-01-08
 **/
public class RocketMqProhibitionTest {
    private static String lineSeparator = System.getProperty("line.separator");

    /**
     * rocketmq场景：测试消费者启动前禁止消费，启动后开启消费，运行时禁止消费，订阅一个Topic
     */
    @Test
    @EnabledIfSystemProperty(named = "mq.consume.prohibition.integration.test.type", matches = "ROCKETMQ_ONE_TOPIC")
    public void testRocketMqOneTopic() throws Exception {
        // 下发禁止消费配置
        String configOn = "enableRocketMqProhibition: true" + lineSeparator +
                "rocketMqTopics:" + lineSeparator +
                " - topic-push-1" + lineSeparator +
                " - topic-pull-subscribe-1" + lineSeparator +
                " - topic-pull-assign-1";
        String configOff = "enableRocketMqProhibition: false" + lineSeparator +
                "rocketMqTopics:" + lineSeparator +
                " - topic-push-1" + lineSeparator +
                " - topic-pull-subscribe-1" + lineSeparator +
                " - topic-pull-assign-1";
        DynamicConfigUtils.updateConfig(configOn);

        // 等待动态配置生效
        Thread.sleep(10000);

        // 消费者生产消息
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-push-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-subscribe-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-assign-1");

        // 启动消费者
        HttpRequestUtils.doGet("http://127.0.0.1:9058/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9056/initAndStart");

        // 判断消费者是否退出消费者组
        checkConsumerAndQueue("0","0", "0", "消费者未退出消费者组.");

        // 消费者开启消费
        DynamicConfigUtils.updateConfig(configOff);

        // 等待动态配置生效
        Thread.sleep(5000);

        // 判断消费者是否加入消费者组
        checkConsumerAndQueue("1","1", "4", "消费者未加入消费者组.");


        // 运行时禁止消费，下发禁止消费配置
        DynamicConfigUtils.updateConfig(configOn);

        // 等待动态配置生效
        Thread.sleep(5000);

        // 判断消费者是否退出消费者组
        checkConsumerAndQueue("0","0", "0", "消费者未退出消费者组.");
    }

    /**
     * rocketmq场景：测试消费者启动前禁止消费，启动后开启消费，运行时禁止消费，订阅两个Topic
     */
    @Test
    @EnabledIfSystemProperty(named = "mq.consume.prohibition.integration.test.type", matches = "ROCKETMQ_TWO_TOPIC")
    public void testRocketMqTwoTopic() throws Exception {
        // 消费者生产消息
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-push-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-push-2");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-subscribe-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-subscribe-2");


        // 启动消费者
        HttpRequestUtils.doGet("http://127.0.0.1:9058/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/initAndStart");

        // 下发禁止消费配置
        String config = "enableRocketMqProhibition: true" + lineSeparator +
                "rocketMqTopics:" + lineSeparator +
                " - topic-push-2" + lineSeparator +
                " - topic-pull-subscribe-2";
        DynamicConfigUtils.updateConfig(config);

        // 等待动态配置生效
        Thread.sleep(5000);

        // 判断消费者是否加入消费者组
        checkConsumer("1", "1", "消费者未加入消费者组.");

        // 消费者订阅新的Topic
        HttpRequestUtils.doGet("http://127.0.0.1:9058/subscribe?topic=topic-push-2");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/subscribe?topic=topic-pull-subscribe-2");


        // 判断消费者是否退出消费者组
        checkConsumer("0", "0", "消费者未退出消费者组.");

        // 消费者取消订阅新的Topic
        HttpRequestUtils.doGet("http://127.0.0.1:9058/unsubscribe?topic=topic-push-2");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/unsubscribe?topic=topic-pull-subscribe-2");

        // 判断消费者是否加入消费者组
        checkConsumer("1", "1", "消费者未加入消费者组.");
    }

    /**
     * rocketmq场景：测试两个消费者禁止消费和开启消费，订阅一个Topic
     */
    @Test
    @EnabledIfSystemProperty(named = "mq.consume.prohibition.integration.test.type", matches = "ROCKETMQ_TWO_CONSUMER")
    public void testRocketMqTwoConsumer() throws Exception {
        // 下发禁止消费配置
        String configOn = "enableRocketMqProhibition: true" + lineSeparator +
                "rocketMqTopics:" + lineSeparator +
                " - topic-push-1" + lineSeparator +
                " - topic-pull-subscribe-1" + lineSeparator +
                " - topic-pull-assign-1";
        String configOff = "enableRocketMqProhibition: false" + lineSeparator +
                "rocketMqTopics:" + lineSeparator +
                " - topic-push-1" + lineSeparator +
                " - topic-pull-subscribe-1" + lineSeparator +
                " - topic-pull-assign-1";

        // 消费者生产消息
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-push-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-subscribe-1");

        // 启动消费者
        HttpRequestUtils.doGet("http://127.0.0.1:9058/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9060/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9056/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/initAndStart");

        // 判断消费者是否加入消费者组
        checkConsumer("2","2", "消费者未加入消费者组.");

        // 开启禁消费
        DynamicConfigUtils.updateConfig(configOn);

        // 等待动态配置生效
        Thread.sleep(5000);

        // 判断消费者是否退出消费者组
        checkConsumer("1","1", "消费者未退出消费者组.");


        // 消费者开启消费
        DynamicConfigUtils.updateConfig(configOff);

        // 等待动态配置生效
        Thread.sleep(5000);

        // 判断消费者是否加入消费者组
        checkConsumer("2","2", "消费者未加入消费者组.");
    }

    /**
     * 校验消费者数量
     *
     * @param pushGroupConsumerCount group为push-group的消费者数量
     * @param pullSubscribeGroupConsumerCount group为pull-subscribe-group的消费者数量
     * @param message 失败信息
     * @throws InterruptedException 线程中断异常
     */
    private void checkConsumer(String pushGroupConsumerCount, String pullSubscribeGroupConsumerCount, String message)
            throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            Thread.sleep(20000);
            String consumerIdListCount1 = HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                    + "?topic=topic-push-1&&group=push-group");
            String consumerIdListCount2 = HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                    + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group");
            if (pushGroupConsumerCount.equals(consumerIdListCount1)
                    && pullSubscribeGroupConsumerCount.equals(consumerIdListCount2)) {
                break;
            }
            if (i == 14) {
                Assertions.fail(message);
            }
        }
    }

    /**
     * 校验消费者数量
     *
     * @param pushGroupConsumerCount group为push-group的消费者数量
     * @param pullSubscribeGroupConsumerCount group为pull-subscribe-group的消费者数量
     * @param queueCount 消息队列数量
     * @param message 失败信息
     * @throws InterruptedException 线程中断异常
     */
    private void checkConsumerAndQueue(String pushGroupConsumerCount, String pullSubscribeGroupConsumerCount,
                                       String queueCount, String message) throws InterruptedException {
        for (int i = 0; i < 25; i++) {
            Thread.sleep(20000);
            String consumerIdListCount1 = HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList?" +
                    "topic=topic-push-1&&group=push-group");
            String consumerIdListCount2 = HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList?" +
                    "topic=topic-pull-subscribe-1&&group=pull-subscribe-group");
            String messageQueueCount = HttpRequestUtils.doGet("http://127.0.0.1:9056/messageQueue");
            if (pushGroupConsumerCount.equals(consumerIdListCount1)
                    && pullSubscribeGroupConsumerCount.equals(consumerIdListCount2)
                    && queueCount.equals(messageQueueCount)) {
                break;
            }
            if (i == 19) {
                Assertions.fail(message);
            }
        }
    }
}
