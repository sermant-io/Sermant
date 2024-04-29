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

package io.sermant.tag.transmission.rocketmqv5.interceptor;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import io.sermant.tag.transmission.utils.RocketmqProducerMarkUtils;

import org.apache.rocketmq.common.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RocketMQ Consumer interceptor for transparent transmission of traffic tags, supports RocketMQ5.x
 *
 * @author tangle
 * @since 2023-07-19
 */
public class RocketmqConsumerInterceptor extends AbstractServerInterceptor<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The class name of the getBody method
     */
    private static final String ROCKETMQ_SELECT_CLASSNAME = "org.apache.rocketmq.common.message.Message";

    /**
     * The prefix the getBody method should be filtered by
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

        // Message queue consumers do not remove thread variables and need to set new objects each time to
        // ensure variable isolation between parent and child threads
        TrafficUtils.setTrafficTag(new TrafficTag(tagMap));
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Parse traffic tag from Message
     *
     * @param message RocketMQ consumer traffic tag carrier
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
     * Determines whether the current call stack matches the intercept point entry requirements
     *
     * @param stackTraceElements stack
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
