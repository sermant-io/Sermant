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

package com.huaweicloud.sermant.mq.prohibition.integration.rocketmq;

import com.huaweicloud.sermant.mq.prohibition.integration.utils.DynamicConfigUtils;
import com.huaweicloud.sermant.mq.prohibition.integration.utils.HttpRequestUtils;

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
        Thread.sleep(3000);

        // 消费者生产消息
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-push-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-subscribe-1");
        HttpRequestUtils.doGet("http://127.0.0.1:9059/produce?topic=topic-pull-assign-1");

        // 启动消费者
        HttpRequestUtils.doGet("http://127.0.0.1:9058/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/initAndStart");
        HttpRequestUtils.doGet("http://127.0.0.1:9056/initAndStart");

        // 等待消费者启动
        Thread.sleep(50000);

        // 判断消费者是否退出消费者组
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9056/messageQueue"));

        // 消费者开启消费
        DynamicConfigUtils.updateConfig(configOff);

        // 等待动态配置生效
        Thread.sleep(120000);

        // 判断消费者是否加入消费者组
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));
        Assertions.assertEquals("4", HttpRequestUtils.doGet("http://127.0.0.1:9056/messageQueue"));

        // 运行时禁止消费，下发禁止消费配置
        DynamicConfigUtils.updateConfig(configOn);

        // 等待动态配置生效
        Thread.sleep(10000);

        // 判断消费者是否退出消费者组
        Assertions.assertEquals("0",
                HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("0", HttpRequestUtils
                .doGet("http://127.0.0.1:9057/consumerIdList?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9056/messageQueue"));
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
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));

        // 消费者订阅新的Topic
        HttpRequestUtils.doGet("http://127.0.0.1:9058/subscribe?topic=topic-push-2");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/subscribe?topic=topic-pull-subscribe-2");

        // 等待消费者退出消费者组
        Thread.sleep(25000);

        // 判断消费者是否退出消费者组
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-2&&group=push-group"));
        Assertions.assertEquals("0", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-2&&group=pull-subscribe-group"));

        // 消费者取消订阅新的Topic
        HttpRequestUtils.doGet("http://127.0.0.1:9058/unsubscribe?topic=topic-push-2");
        HttpRequestUtils.doGet("http://127.0.0.1:9057/unsubscribe?topic=topic-pull-subscribe-2");

        // 等待消费者加入消费者组
        Thread.sleep(25000);

        // 判断消费者是否加入消费者组
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));
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

        // 等待消费者启动
        Thread.sleep(50000);

        // 判断消费者是否加入消费者组
        Assertions.assertEquals("2", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("2", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));

        // 开启禁消费
        DynamicConfigUtils.updateConfig(configOn);

        // 等待动态配置生效
        Thread.sleep(25000);

        // 判断消费者是否退出消费者组
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("1", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));


        // 消费者开启消费
        DynamicConfigUtils.updateConfig(configOff);

        // 等待动态配置生效
        Thread.sleep(25000);

        // 判断消费者是否加入消费者组
        Assertions.assertEquals("2", HttpRequestUtils.doGet("http://127.0.0.1:9058/consumerIdList"
                + "?topic=topic-push-1&&group=push-group"));
        Assertions.assertEquals("2", HttpRequestUtils.doGet("http://127.0.0.1:9057/consumerIdList"
                + "?topic=topic-pull-subscribe-1&&group=pull-subscribe-group"));
    }
}