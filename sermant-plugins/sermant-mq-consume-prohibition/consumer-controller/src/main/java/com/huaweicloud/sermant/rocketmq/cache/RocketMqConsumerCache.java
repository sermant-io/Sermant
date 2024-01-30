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

package com.huaweicloud.sermant.rocketmq.cache;

import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rocketmq消费者缓存
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqConsumerCache {
    /**
     * push消费者wrapper缓存
     */
    public static final Map<Integer, DefaultMqPushConsumerWrapper> PUSH_CONSUMERS_CACHE =
            new ConcurrentHashMap<>();

    /**
     * pull消费者wrapper缓存
     */
    public static final Map<Integer, DefaultLitePullConsumerWrapper> PULL_CONSUMERS_CACHE =
            new ConcurrentHashMap<>();

    private RocketMqConsumerCache() {
    }
}