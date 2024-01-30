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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.config.ProhibitionConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;
import com.huaweicloud.sermant.rocketmq.extension.RocketMqConsumerHandler;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * pullconsumer抽象拦截器
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public abstract class AbstractPullConsumerInterceptor extends AbstractInterceptor {
    /**
     * 外部扩展处理器
     */
    protected RocketMqConsumerHandler handler;

    /**
     * 无参构造方法
     */
    public AbstractPullConsumerInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 外部扩展处理器
     */
    public AbstractPullConsumerInterceptor(RocketMqConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * pullconsumer 执行禁消费操作
     *
     * @param pullConsumerWrapper pullconsumer包装类实例
     */
    protected void disablePullConsumption(DefaultLitePullConsumerWrapper pullConsumerWrapper) {
        if (pullConsumerWrapper != null) {
            RocketMqPullConsumerController.disablePullConsumption(pullConsumerWrapper,
                    ProhibitionConfigManager.getRocketMqProhibitionTopics());
        }
    }

    /**
     * 获取消息队列的topic
     *
     * @param messageQueues 消息队列
     * @return 消息队列的topic
     */
    protected Set<String> getMessageQueueTopics(Collection<MessageQueue> messageQueues) {
        HashSet<String> topics = new HashSet<>();
        for (MessageQueue messageQueue : messageQueues) {
            topics.add(messageQueue.getTopic());
        }
        return topics;
    }
}
