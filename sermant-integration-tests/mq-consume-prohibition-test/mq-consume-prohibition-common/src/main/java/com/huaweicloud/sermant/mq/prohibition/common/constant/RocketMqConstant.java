/*
 *  Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.mq.prohibition.common.constant;

/**
 * rocketmq常量
 *
 * @author daizhenyu
 * @since 2024-01-08
 **/
public class RocketMqConstant {
    /**
     * 消息中间件的topic
     */
    public static final String PUSH_CONSUME_TOPIC = "topic-push-1";

    /**
     * 消息中间件的topic
     */
    public static final String PULL_SUBSCRIBE_CONSUME_TOPIC = "topic-pull-subscribe-1";

    /**
     * 消息中间件的topic
     */
    public static final String PULL_ASSIGN_CONSUME_TOPIC = "topic-pull-assign-1";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * rocketmq消息的tag
     */
    public static final String TAG = "";

    /**
     * rocketmq消费者消费消息的标签范围
     */
    public static final String TAG_SCOPE = "*";

    /**
     * rocketmq生产者组
     */
    public static final String PRODUCE_GROUP = "producer-group";

    /**
     * rocketmq push消费者组
     */
    public static final String PUSH_CONSUME_GROUP = "push-group";

    /**
     * rocketmq pull subscribe消费者组
     */
    public static final String PULL_SUBSCRIBE_CONSUME_GROUP = "pull-subscribe-group";

    /**
     * rocketmq pull assign消费者组
     */
    public static final String PULL_ASSIGN_CONSUME_GROUP = "pull-assign-group";

    /**
     * rocketmq消息体
     */
    public static final String MESSAGE_BODY_ROCKET = "hello inner rocketmq:";

    private RocketMqConstant() {
    }
}
