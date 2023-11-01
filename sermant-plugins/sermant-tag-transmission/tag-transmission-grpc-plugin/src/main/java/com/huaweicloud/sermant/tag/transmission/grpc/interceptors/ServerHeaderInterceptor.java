/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.grpc.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * grpc内部的server interceptor，从grpc的header中提取流量标签
 *
 * @author daizhenyu
 * @since 2023-08-15
 **/
public class ServerHeaderInterceptor implements ServerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 流量标签透传配置类
     */
    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造方法
     */
    public ServerHeaderInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    /**
     * 使用grpc提供的server端拦截器获取header中的流量标签
     *
     * @param call 服务端call
     * @param requestHeaders 请求头
     * @param next call处理器
     * @return ServerCall.Listener 监听器
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {
        // 处理header
        if (requestHeaders != null) {
            extractTrafficTagFromCarrier(requestHeaders);
        }
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
        }, requestHeaders);
    }

    private void extractTrafficTagFromCarrier(Metadata requestHeaders) {
        Map<String, List<String>> tag = new HashMap<>();
        Set<String> keySet = requestHeaders.keys();
        for (String key : keySet) {
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            String value = requestHeaders.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));

            // 流量标签的value为null时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
            if (value == null || "null".equals(value)) {
                tag.put(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0}=null have been extracted from grpc.", key);
                continue;
            }
            tag.put(key, Collections.singletonList(value));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been extracted from grpc.", new Object[]{key,
                    value});
        }
        TrafficUtils.updateTrafficTag(tag);
    }
}
