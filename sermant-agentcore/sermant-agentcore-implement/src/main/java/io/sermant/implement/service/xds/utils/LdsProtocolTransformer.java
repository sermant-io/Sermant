/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.xds.utils;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Convert lds protocol data to Sermant data model
 *
 * @author daizhenyu
 * @since 2024-08-22
 **/
public class LdsProtocolTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private LdsProtocolTransformer() {
    }

    /**
     * get HttpConnectionManager
     *
     * @param listeners listeners
     * @return HttpConnectionManager list
     */
    public static List<XdsHttpConnectionManager> getHttpConnectionManager(List<Listener> listeners) {
        return listeners.stream()
                .filter(Objects::nonNull)
                .flatMap(listener -> listener.getFilterChainsList().stream())
                .flatMap(e -> e.getFiltersList().stream())
                .map(Filter::getTypedConfig)
                .map(LdsProtocolTransformer::unpackHttpConnectionManager)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(httpConnectionManager -> {
                    XdsHttpConnectionManager xdsHcm = new XdsHttpConnectionManager();
                    xdsHcm.setRouteConfigName(httpConnectionManager.getRds().getRouteConfigName());
                    return xdsHcm;
                })
                .collect(Collectors.toList());
    }

    private static Optional<HttpConnectionManager> unpackHttpConnectionManager(Any any) {
        try {
            if (!any.is(HttpConnectionManager.class)) {
                return Optional.empty();
            }
            return Optional.of(any.unpack(HttpConnectionManager.class));
        } catch (InvalidProtocolBufferException e) {
            LOGGER.log(Level.SEVERE, "Decode resource to HttpConnectionManager failed.", e);
            return Optional.empty();
        }
    }
}
