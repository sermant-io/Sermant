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

package io.sermant.tag.transmission.grpc.interceptors;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.TagTransmissionConfig;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * grpc ServerHeaderInterceptor，extract traffic labels from the header of the grpc
 *
 * @author daizhenyu
 * @since 2023-08-15
 **/
public class ServerHeaderInterceptor implements ServerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Traffic label transparent transmission configuration class
     */
    protected final TagTransmissionConfig tagTransmissionConfig;

    /**
     * construction method
     */
    public ServerHeaderInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    /**
     * Get the traffic label in the header using the server side interceptor provided by grpc
     *
     * @param call server call
     * @param requestHeaders request headers
     * @param next call
     * @return ServerCall.Listener listener
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {
        // handle header
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

            // When the value of the traffic label is null, it also needs to be stored in a local variable to
            // overwrite the original value to prevent misuse of the old traffic label.
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
