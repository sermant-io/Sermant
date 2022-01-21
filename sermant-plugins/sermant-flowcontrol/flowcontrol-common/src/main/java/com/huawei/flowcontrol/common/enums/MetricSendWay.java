/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.enums;

import com.huawei.flowcontrol.common.metric.MetricSender;
import com.huawei.flowcontrol.common.metric.NettyMetricSender;
import com.huawei.flowcontrol.common.metric.SimpleKafkaMetricSender;

/**
 * 指标数据发送方式
 *
 * @author zhouss
 * @since 2021-12-07
 */
public enum MetricSendWay {
    /**
     * Kafka发送方式
     */
    KAFKA(new SimpleKafkaMetricSender()),

    /**
     * Netty发送方式
     */
    NETTY(new NettyMetricSender());

    private final MetricSender sender;

    MetricSendWay(MetricSender sender) {
        this.sender = sender;
    }

    public MetricSender getSender() {
        return sender;
    }
}
