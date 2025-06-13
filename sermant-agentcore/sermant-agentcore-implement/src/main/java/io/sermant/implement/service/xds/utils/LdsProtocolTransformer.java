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
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsPeerAuthenticationPolicy;

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

    private static final String VIRTUAL_INBOUND = "virtualInbound";

    private static final String ENVOY_TRANSPORT_SOCKETS_TLS = "envoy.transport_sockets.tls";

    private static final String ENVOY_TRANSPORT_SOCKETS_RAW_BUFFER = "envoy.transport_sockets.raw_buffer";

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

    /**
     * Update PeerAuthentication Policy
     *
     * @param listeners listeners
     * @return Peer Authentication Policy
     * @throws SermantRuntimeException Parse DownstreamTlsContext Error
     */
    public static XdsPeerAuthenticationPolicy updatePeerAuthenticationPolicy(List<Listener> listeners) {
        boolean supportPlainText = false;
        boolean supportSsl = false;
        for (Listener listener : listeners) {
            if (!VIRTUAL_INBOUND.equals(listener.getName())) {
                continue;
            }
            List<FilterChain> filterChains = listener.getFilterChainsList();
            for (FilterChain filterChain : filterChains) {
                if (ENVOY_TRANSPORT_SOCKETS_TLS.equals(filterChain.getTransportSocket().getName())) {
                    DownstreamTlsContext tlsContext;
                    try {
                        tlsContext = DownstreamTlsContext.parseFrom(
                                filterChain.getTransportSocket().getTypedConfig().getValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new SermantRuntimeException("Parse DownstreamTlsContext Error", e);
                    }
                    boolean requireClientCert = tlsContext.getRequireClientCertificate().getValue();
                    boolean hasServerCert =
                            tlsContext.getCommonTlsContext().getTlsCertificateSdsSecretConfigsCount() > 0;
                    boolean hasValidationContext = tlsContext.getCommonTlsContext()
                            .hasValidationContextSdsSecretConfig();
                    supportSsl = requireClientCert && hasServerCert && hasValidationContext;
                }
                if (ENVOY_TRANSPORT_SOCKETS_RAW_BUFFER.equals(
                        filterChain.getTransportSocket().getName())) {
                    supportPlainText = true;
                }
            }
        }
        if (supportSsl && !supportPlainText) {
            return XdsPeerAuthenticationPolicy.STRICT;
        }
        if (supportSsl && supportPlainText) {
            return XdsPeerAuthenticationPolicy.PERMISSIVE;
        }
        if (!supportSsl) {
            return XdsPeerAuthenticationPolicy.DISABLE;
        }
        return XdsPeerAuthenticationPolicy.UNSET;
    }
}
