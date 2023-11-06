/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.tagtransmission.kafka.consumer.controller;

import com.huaweicloud.demo.tagtransmission.kafka.consumer.KafkaWithConsumer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息中间件消费者controller
 *
 * @author daizhenyu
 * @since 2023-09-28
 **/
@RestController
@RequestMapping(value = "kafkaConsumer")
public class KafkaConsumerController {
    /**
     * 查询kafka消费者消费消息后返回的流量标签透传
     *
     * @return string
     */
    @RequestMapping(value = "queryKafkaTag", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String queryKafkaTag() {
        String trafficTag = KafkaWithConsumer.KAFKA_TAG_MAP.get("kafkaTag");

        // 删除流量标签，以免干扰下一次测试查询
        KafkaWithConsumer.KAFKA_TAG_MAP.remove("kafkaTag");
        return trafficTag;
    }
}
