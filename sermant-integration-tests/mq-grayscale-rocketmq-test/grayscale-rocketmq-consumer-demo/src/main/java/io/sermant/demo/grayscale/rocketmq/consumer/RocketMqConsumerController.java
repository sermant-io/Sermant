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

package io.sermant.demo.grayscale.rocketmq.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * consumer controller
 *
 * @author chengyouling
 * @since 2024-10-30
 **/
@RestController
public class RocketMqConsumerController {
    @Value("${rocketmq.address}")
    private String nameServer;

    @Value("${rocketmq.topic}")
    private String topic;

    @Autowired
    private RocketMqPullConsumer pullConsumer;

    @Autowired
    private RocketMqPushConsumer pushConsumer;

    @Autowired
    private RocketMqLitePullConsumer litePullConsumer;

    /**
     * clear cache count info
     *
     * @return is success
     */
    @GetMapping("/clearMessageCount")
    public String clearMessageCount() {
        RocketMqMessageUtils.clearMessageCount();
        return "success";
    }

    /**
     * init consumer
     *
     * @param consumerType consumerType
     * @return init status
     */
    @GetMapping("/initConsumer")
    public String initConsumer(@RequestParam("consumerType") String consumerType) {
        if ("PUSH".equals(consumerType)) {
            pushConsumer.initPushConsumer(topic + "-PUSH", nameServer);
        } else if ("PULL".equals(consumerType)) {
            pullConsumer.initPullConsumer(nameServer, topic + "-PULL");
        } else {
            litePullConsumer.initLitePullConsumer(topic + "-LITE-PULL", nameServer);
        }
        return "success";
    }

    /**
     * shutdown consumer
     *
     * @param consumerType consumerType
     * @return status
     */
    @GetMapping("/shutdownConsumer")
    public String shutdownConsumer(@RequestParam("consumerType") String consumerType) {
        if ("PUSH".equals(consumerType)) {
            pushConsumer.shutdownPushConsumer();
        } else if ("PULL".equals(consumerType)) {
            pullConsumer.shutdownPullConsumer();
        } else {
            litePullConsumer.shutdownLitePullConsumer();
        }
        return "ok";
    }

    /**
     * pull message
     *
     * @return message count
     */
    @GetMapping("/getPullConsumerMessageCount")
    public Map<String, Object> getPullConsumerMessageCount() {
        return pullConsumer.getMessageCount();
    }

    /**
     * get push consumer message count
     *
     * @return message count
     */
    @GetMapping("/getPushConsumerMessageCount")
    public Map<String, Object> getPushConsumerMessageCount() {
        return pushConsumer.getMessageCount(topic + "-PUSH", nameServer);
    }

    /**
     * get lite pull consumer message count
     *
     * @return message count
     */
    @GetMapping("/getLitePullConsumerMessageCount")
    public Map<String, Object> getLitePullConsumerMessageCount() {
        return litePullConsumer.getMessageCount(topic + "-LITE-PULL", nameServer);
    }
}
