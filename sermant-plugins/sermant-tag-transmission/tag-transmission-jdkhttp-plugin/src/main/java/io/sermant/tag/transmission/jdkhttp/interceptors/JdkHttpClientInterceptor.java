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

package io.sermant.tag.transmission.jdkhttp.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractClientInterceptor;
import sun.net.www.MessageHeader;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDK HttpClient Interceptor for transparent transmission of traffic tag
 *
 * @author lilai
 * @since 2023-08-08
 */
public class JdkHttpClientInterceptor extends AbstractClientInterceptor<MessageHeader> {
    /**
     * Filter multiple calls of interceptors during a single processing
     */
    protected static final ThreadLocal<Boolean> LOCK_MARK = new ThreadLocal<>();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (LOCK_MARK.get() != null) {
            return context;
        }
        LOCK_MARK.set(Boolean.TRUE);

        Object messageHeaderObject = context.getArguments()[0];
        if (!(messageHeaderObject instanceof MessageHeader)) {
            return context;
        }

        injectTrafficTag2Carrier((MessageHeader) messageHeaderObject);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        LOCK_MARK.remove();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        LOCK_MARK.remove();
        return context;
    }

    /**
     * adds a traffic tag to MessageHeader
     *
     * @param messageHeader Jdk HttpClient tag transfer carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(MessageHeader messageHeader) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }

            // if original headers contains the specific key, then ignore
            Map<String, List<String>> originalHeaders = messageHeader.getHeaders();
            if (!CollectionUtils.isEmpty(originalHeaders.get(key))) {
                continue;
            }

            List<String> values = entry.getValue();

            // The server side converts the label value to list storage when it is not null. If it is null, it directly
            // puts null. Therefore, if the client side values are empty, they must be null.
            if (CollectionUtils.isEmpty(values)) {
                messageHeader.add(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been injected to jdkhttp.", entry);
                continue;
            }
            for (String value : values) {
                messageHeader.add(key, value);
            }
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to httpclient.", new Object[]{key,
                    values});
        }
    }
}
