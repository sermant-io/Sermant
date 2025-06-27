/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

package io.sermant.demo.tagtransmission.rabbitmq.consumer.controller;

import io.sermant.demo.tagtransmission.rabbitmq.consumer.RabbitMqConsumer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * RabbitMQ message middleware consumer controller
 *
 * @since 2025-06-27
 */
@RestController
@RequestMapping(value = "rabbitMqConsumer")
public class RabbitMqConsumerController {

    /**
     * Tag key
     */
    public static final String RABBITMQ_TAG = "rabbitmqTag";

    /**
     * Query traffic tag transmission returned by RabbitMQ consumer after consuming messages
     *
     * @return traffic tag string
     */
    @RequestMapping(value = "queryRabbitMqTag", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String queryRabbitMqTag() {
        String trafficTag = RabbitMqConsumer.RABBITMQ_TAG_MAP.get(RABBITMQ_TAG);

        // Remove traffic tag to avoid interfering with next test query
        RabbitMqConsumer.RABBITMQ_TAG_MAP.remove(RABBITMQ_TAG);
        return trafficTag;
    }
}
