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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

import org.apache.rocketmq.common.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RocketMQ流量标签透传的消费者拦截器，支持RocketMQ4.8+
 *
 * @author tangle
 * @since 2023-07-19
 */
public class RocketmqConsumerInterceptor extends AbstractInterceptor {
    /**
     * getBody拦截方法的所在类名
     */
    private static final String ROCKETMQ_SELECT_CLASSNAME = "org.apache.rocketmq.common.message.Message";

    /**
     * getBody拦截方法应被过滤的前缀
     */
    private static final String ROCKETMQ_FILER_PREFIX = "org.apache.rocketmq";

    private final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
     */
    public RocketmqConsumerInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!tagTransmissionConfig.isEnabled()) {
            return context;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int stackTraceIdxMax = stackTraceElements.length - 1;
        for (int i = 0; i < stackTraceElements.length; i++) {
            if (!ROCKETMQ_SELECT_CLASSNAME.equals(stackTraceElements[i].getClassName())) {
                continue;
            }
            if (i == stackTraceIdxMax || stackTraceElements[i + 1].getClassName()
                    .startsWith(ROCKETMQ_FILER_PREFIX)) {
                return context;
            }
        }
        if (context.getObject() instanceof Message) {
            Message message = (Message) context.getObject();
            Map<String, List<String>> tag = this.getTagFromMessage(message);
            TrafficUtils.updateTrafficTag(tag);
        }
        return context;
    }

    /**
     * 获取message中的流量标签
     *
     * @param message 原始properties
     * @return Map
     */
    private Map<String, List<String>> getTagFromMessage(Message message) {
        Map<String, List<String>> tag = new HashMap<>();
        for (String key : tagTransmissionConfig.getTagKeys()) {
            String value = message.getProperty(key);
            if (value != null) {
                tag.put(key, Collections.singletonList(value));
            } else {
                tag.put(key, null);
            }
        }
        return tag;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
