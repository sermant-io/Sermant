/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
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
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.entity.XdsAuthorizationRule;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsJwtRule;
import io.sermant.core.service.xds.entity.XdsPeerAuthenticationPolicy;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    private static final String HTTP_CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";

    private static final String LDS_JWT_FILTER = "envoy.filters.http.jwt_authn";

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

    static Optional<HttpConnectionManager> unpackHttpConnectionManager(Any any) {
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
     * @throws SermantRuntimeException Parse DownstreamTlsContext Error
     */
    public static void resolvePeerAuthenticationPolicy(List<Listener> listeners) {
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
                if (ENVOY_TRANSPORT_SOCKETS_RAW_BUFFER.equals(filterChain.getTransportSocket().getName())) {
                    supportPlainText = true;
                }
            }
        }
        XdsPeerAuthenticationPolicy policy = XdsPeerAuthenticationPolicy.UNSET;
        if (supportSsl && !supportPlainText) {
            policy = XdsPeerAuthenticationPolicy.STRICT;
        }
        if (supportSsl && supportPlainText) {
            policy = XdsPeerAuthenticationPolicy.PERMISSIVE;
        }
        if (!supportSsl) {
            policy = XdsPeerAuthenticationPolicy.DISABLE;
        }
        XdsDataCache.updatePeerAuthenticationPolicy(policy);
    }

    /**
     * Resolve authorization rules
     *
     * @param listeners listeners from istiod
     */
    public static void resolveAuthorizationRule(List<Listener> listeners) {
        List<HttpFilter> httpFilters = resolveHttpFilter(listeners);
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }

        XdsAuthorizationRule rule = new XdsAuthorizationRule();
        Map<String, XdsJwtRule> xdsJwtRules = resolveJwt(httpFilters);
        rule.setJwtRules(xdsJwtRules);
        XdsDataCache.updateXdsAuthorizationRule(rule);
    }

    /**
     * Resolve http filters in listeners
     *
     * @param listeners listeners from istiod
     * @return list of listeners
     */
    public static List<HttpFilter> resolveHttpFilter(List<Listener> listeners) {
        if (listeners == null) {
            return Collections.emptyList();
        }

        return listeners.stream().filter(listener -> listener != null && VIRTUAL_INBOUND.equals(listener.getName()))
                .flatMap(listener -> listener.getFilterChainsList().stream()).filter(Objects::nonNull)
                .flatMap(filterChain -> filterChain.getFiltersList().stream())
                .filter(filter -> filter != null && HTTP_CONNECTION_MANAGER.equals(filter.getName()))
                .map(filter -> unpackHttpConnectionManager(filter.getTypedConfig())).filter(Optional::isPresent)
                .flatMap(optional -> optional.get().getHttpFiltersList().stream()).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Resolve Jwt in http filters
     *
     * @param httpFilters http filters
     * @return Map of XdsJwtRule
     */
    public static Map<String, XdsJwtRule> resolveJwt(List<HttpFilter> httpFilters) {
        if (CollectionUtils.isEmpty(httpFilters)) {
            return Collections.emptyMap();
        }
        JwtAuthentication jwtAuthentication = null;
        for (HttpFilter httpFilter : httpFilters) {
            if (!httpFilter.getName().equals(LDS_JWT_FILTER)) {
                continue;
            }
            try {
                jwtAuthentication = httpFilter.getTypedConfig().unpack(JwtAuthentication.class);
                if (jwtAuthentication != null) {
                    break;
                }
            } catch (InvalidProtocolBufferException e) {
                LOGGER.log(Level.SEVERE, "Resolve Jwt Rule error", e);
            }
        }
        if (jwtAuthentication == null) {
            return Collections.emptyMap();
        }

        Map<String, JwtProvider> jwtProviders = jwtAuthentication.getProvidersMap();
        return parseXdsJwtRuleMapFromJwtProvider(jwtProviders);
    }

    private static Map<String, XdsJwtRule> parseXdsJwtRuleMapFromJwtProvider(Map<String, JwtProvider> jwtProviders) {
        Map<String, XdsJwtRule> xdsJwtRule = new HashMap<>(jwtProviders.size());
        for (Entry<String, JwtProvider> entry : jwtProviders.entrySet()) {
            JwtProvider provider = entry.getValue();
            Map<String, String> fromHeaders = new HashMap<>();
            for (JwtHeader header : provider.getFromHeadersList()) {
                fromHeaders.put(header.getName(), header.getValuePrefix());
            }
            List<String> audiences = provider.getAudiencesList();
            List<String> fromParams = provider.getFromParamsList();
            String localJwks = provider.hasLocalJwks() ? provider.getLocalJwks().getInlineString() : "";

            XdsJwtRule jwtRule = new XdsJwtRule();
            jwtRule.setAudiences(audiences);
            jwtRule.setFromHeaders(fromHeaders);
            jwtRule.setFromParams(fromParams);
            jwtRule.setIssuer(provider.getIssuer());
            jwtRule.setJwks(localJwks);
            jwtRule.setName(entry.getKey());
            xdsJwtRule.put(entry.getKey(), jwtRule);
        }
        return xdsJwtRule;
    }
}
