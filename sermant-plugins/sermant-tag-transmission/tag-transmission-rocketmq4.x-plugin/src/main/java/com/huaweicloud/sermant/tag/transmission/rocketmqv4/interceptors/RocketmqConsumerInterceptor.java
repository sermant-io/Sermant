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

package com.huaweicloud.sermant.tag.transmission.rocketmqv4.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import com.huaweicloud.sermant.tag.transmission.utils.RocketmqProducerMarkUtils;

import org.apache.rocketmq.common.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RocketMQ流量标签透传的消费者拦截器，支持RocketMQ4.x
 *
 * @author tangle
 * @since 2023-07-19
 */
public class RocketmqConsumerInterceptor extends AbstractServerInterceptor<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * getBody拦截方法的所在类名
     */
    private static final String ROCKETMQ_SELECT_CLASSNAME = "org.apache.rocketmq.common.message.Message";

    /**
     * getBody拦截方法应被过滤的前缀
     */
    private static final String ROCKETMQ_FILER_PREFIX = "org.apache.rocketmq";

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (RocketmqProducerMarkUtils.isProducer()) {
            return context;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (!isRocketMqStackTrace(stackTraceElements)) {
            return context;
        }
        if (!(context.getObject() instanceof Message)) {
            return context;
        }

        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier((Message) context.getObject());

        // 消息队列消费者不会remove线程变量，需要每次set新对象，以保证父子线程之间的变量隔离
        TrafficUtils.setTrafficTag(new TrafficTag(tagMap));
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * 从Message中解析流量标签
     *
     * @param message RocketMQ 消费端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(Message message) {
        Map<String, List<String>> tagMap = new HashMap<>();
        if (message.getProperties() == null) {
            return tagMap;
        }
        for (Map.Entry<String, String> entry : message.getProperties().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value == null || "null".equals(value)) {
                tagMap.put(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from rocketmq.",
                        entry);
                continue;
            }
            tagMap.put(key, Collections.singletonList(value));
            LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from rocketmq.",
                    entry);
        }
        return tagMap;
    }

    /**
     * 判断当前调用栈是否匹配拦截点进入的要求
     *
     * @param stackTraceElements 调用栈
     * @return boolean
     */
    private boolean isRocketMqStackTrace(StackTraceElement[] stackTraceElements) {
        int stackTraceIdxMax = stackTraceElements.length - 1;
        for (int i = 0; i < stackTraceElements.length; i++) {
            if (!ROCKETMQ_SELECT_CLASSNAME.equals(stackTraceElements[i].getClassName())) {
                continue;
            }
            if (i == stackTraceIdxMax || stackTraceElements[i + 1].getClassName()
                    .startsWith(ROCKETMQ_FILER_PREFIX)) {
                return false;
            }
            break;
        }
        return true;
    }
}
