/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.kafka.extension;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * KafkaConsumer处理器接口，供外部实现在创建KafkaConsumer时执行相应操作
 *
 * @author lilai
 * @since 2023-12-05
 */
public interface KafkaConsumerHandler {
    /**
     * 拦截点之前的处理
     *
     * @param context 拦截点上下文
     */
    void doBefore(ExecuteContext context);

    /**
     * 拦截点之后的处理
     *
     * @param context 拦截点上下文
     */
    void doAfter(ExecuteContext context);
}
