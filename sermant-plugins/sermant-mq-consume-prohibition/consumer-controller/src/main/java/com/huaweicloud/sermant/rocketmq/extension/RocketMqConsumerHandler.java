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

package com.huaweicloud.sermant.rocketmq.extension;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * RocketmqConsumer处理器接口，供外部实现在rocketmq消费者拦截点执行扩展操作
 *
 * @author daizhenyu
 * @since 2023-12-13
 **/
public interface RocketMqConsumerHandler {
    /**
     * 拦截点前置处理
     *
     * @param context 上下文信息
     */
    void doBefore(ExecuteContext context);

    /**
     * 拦截点后置处理
     *
     * @param context 上下文信息
     */
    void doAfter(ExecuteContext context);

    /**
     * 拦截点异常处理
     *
     * @param context 上下文信息
     */
    void doOnThrow(ExecuteContext context);
}
