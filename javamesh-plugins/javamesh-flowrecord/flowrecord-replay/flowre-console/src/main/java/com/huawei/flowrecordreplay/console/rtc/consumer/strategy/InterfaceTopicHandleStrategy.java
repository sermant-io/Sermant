/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowrecordreplay.console.rtc.consumer.strategy;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

/**
 * 定义该接口的目的是抽象出消息的处理逻辑，不同的消息采取不同处理方式
 *
 * @author hanpeng
 * @since 2021-04-07
 */
@FunctionalInterface
public interface InterfaceTopicHandleStrategy {
    /**
     * 按主题处理record记录
     *
     * @param records 记录数
     */
    void handleRecordByTopic(List<ConsumerRecord<String, String>> records);
}
