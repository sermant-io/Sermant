/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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

/*
 * Based on sentinel-extension/sentinel-datasource-xds
 * /src/main/java/com/alibaba/csp/sentinel/datasource/xds/client/filiter/lds/AuthLdsFilter.java
 * from the Sentinel project.
 */

package io.sermant.implement.service.xds.utils;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.config.rbac.v3.Permission.RuleCase;
import io.envoyproxy.envoy.config.rbac.v3.Policy;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.rbac.v3.Principal.IdentifierCase;
import io.envoyproxy.envoy.config.rbac.v3.Principal.Set;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import io.envoyproxy.envoy.config.rbac.v3.RBAC.Action;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher.PathSegment;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.service.xds.entity.XdsAuthCondition;
import io.sermant.core.service.xds.entity.XdsAuthorizationRule;
import io.sermant.core.service.xds.entity.XdsAuthorizationRule.ChildChainType;
import io.sermant.core.service.xds.entity.XdsAuthorizationType;
import io.sermant.core.service.xds.entity.XdsHttpConnectionManager;
import io.sermant.core.service.xds.entity.XdsJwtRule;
import io.sermant.core.service.xds.entity.XdsPeerAuthenticationPolicy;
import io.sermant.core.service.xds.entity.XdsSecurityRule;
import io.sermant.core.service.xds.entity.match.PortMatcher;
import io.sermant.core.service.xds.entity.match.StringMatcher;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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

    private static final String ENVOY_JWT_FILTER = "envoy.filters.http.jwt_authn";

    private static final String ENVOY_RBAC_FILTER = "envoy.filters.http.rbac";

    private static final String REQUEST_AUTH_AUDIENCE = "request.auth.audiences";

    private static final String REQUEST_AUTH_PRINCIPAL = "request.auth.principal";

    private static final String REQUEST_AUTH_PRESENTER = "request.auth.presenter";

    private static final String REQUEST_AUTH_CLAIMS = "request.auth.claims";

    private static final String HEADER_NAME_AUTHORITY = ":authority";

    private static final String HEADER_NAME_METHOD = ":method";

    private static final int SEGMENT_SIZE = 2;

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

        XdsSecurityRule rule = new XdsSecurityRule();
        Map<String, XdsJwtRule> xdsJwtRules = resolveJwt(httpFilters);
        Map<String, XdsAuthorizationRule> allowAuthRules = new HashMap<>();
        Map<String, XdsAuthorizationRule> denyAuthRules = new HashMap<>();
        resolveRbac(httpFilters, allowAuthRules, denyAuthRules);

        rule.setJwtRules(xdsJwtRules);
        rule.setAllowRules(allowAuthRules);
        rule.setDenyRules(denyAuthRules);
        XdsDataCache.updateXdsSecurityRule(rule);
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
            if (!httpFilter.getName().equals(ENVOY_JWT_FILTER)) {
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

    /**
     * Resolve RBAC from istiod
     *
     * @param httpFilters List of HttpFilter
     * @param allowAuthRules Map of XdsAuthorizationRule
     * @param denyAuthRules Map of XdsAuthorizationRule
     */
    public static void resolveRbac(List<HttpFilter> httpFilters, Map<String, XdsAuthorizationRule> allowAuthRules,
            Map<String, XdsAuthorizationRule> denyAuthRules) {
        Map<Action, RBAC> rbacMap = new EnumMap<>(Action.class);

        httpFilters.stream()
                .filter(filter -> ENVOY_RBAC_FILTER.equals(filter.getName()))
                .forEach(httpFilter -> {
                    try {
                        io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC rbac = httpFilter.getTypedConfig()
                                .unpack(
                                        io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC.class);
                        if (rbac != null && !rbacMap.containsKey(rbac.getRules().getAction())) {
                            rbacMap.put(rbac.getRules().getAction(), rbac.getRules());
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Parse Rbac Rule error");
                    }
                });

        for (Entry<Action, RBAC> rbacEntry : rbacMap.entrySet()) {
            Action action = rbacEntry.getKey();
            RBAC rbac = rbacEntry.getValue();
            for (Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
                XdsAuthorizationRule authRule = new XdsAuthorizationRule(ChildChainType.AND);

                processPrincipalsOrPermissions(entry.getValue().getPrincipalsList(),
                        authRule, principal -> resolvePrincipal((Principal) principal));
                processPrincipalsOrPermissions(entry.getValue().getPermissionsList(),
                        authRule, permission -> resolvePermission((Permission) permission));

                if (authRule.isEmpty()) {
                    continue;
                }
                switch (action) {
                    case ALLOW:
                        allowAuthRules.put(entry.getKey(), authRule);
                        break;
                    case DENY:
                        denyAuthRules.put(entry.getKey(), authRule);
                        break;
                    case UNRECOGNIZED:
                    default:
                        LOGGER.warning("Unknown or unrecognized rbac action: " + action);
                        allowAuthRules.put(entry.getKey(), authRule);
                }
            }
        }
    }

    private static void processPrincipalsOrPermissions(List<?> items, XdsAuthorizationRule authRule,
            Function<Object, XdsAuthorizationRule> resolver) {
        XdsAuthorizationRule orRule = new XdsAuthorizationRule(ChildChainType.OR);
        for (Object item : items) {
            XdsAuthorizationRule andRule = resolver.apply(item);
            if (andRule != null && !andRule.isEmpty()) {
                orRule.addChildren(andRule);
            }
        }
        if (!orRule.isEmpty()) {
            authRule.addChildren(orRule);
        }
    }

    private static XdsAuthorizationRule resolvePrincipal(Principal principal) {
        Set andIds = principal.getAndIds();
        XdsAuthorizationRule andChildren = new XdsAuthorizationRule(ChildChainType.AND);
        for (Principal andId : andIds.getIdsList()) {
            if (andId.getAny()) {
                return new XdsAuthorizationRule();
            }

            boolean isNot = false;
            if (andId.hasNotId()) {
                isNot = true;
                andId = andId.getNotId();
            }

            Set ids;
            XdsAuthorizationRule children;
            if (andId.hasAndIds()) {
                ids = andId.getAndIds();
                children = new XdsAuthorizationRule(ChildChainType.AND, isNot);
            } else if (andId.hasOrIds()) {
                ids = andId.getOrIds();
                children = new XdsAuthorizationRule(ChildChainType.OR, isNot);
            } else {
                return new XdsAuthorizationRule();
            }

            for (Principal orId : ids.getIdsList()) {
                IdentifierCase identifierCase = orId.getIdentifierCase();

                switch (identifierCase) {
                    case HEADER:
                        handleHeaderCase(orId, children);
                        break;
                    case REMOTE_IP:
                        handleRemoteIpCase(orId, children);
                        break;
                    case DIRECT_REMOTE_IP:
                        handleDirectRemoteIpCase(orId, children);
                        break;
                    case METADATA:
                        handleMetadataCase(orId, children);
                        break;
                    default:
                        LOGGER.warning("Unsupported identifierCase:" + identifierCase);
                }
            }

            if (!children.isEmpty()) {
                andChildren.addChildren(children);
            }
        }
        return andChildren;
    }

    private static void handleHeaderCase(Principal orId, XdsAuthorizationRule orChildren) {
        String headerName = orId.getHeader().getName();
        StringMatcher stringMatcher = MatcherUtil.convertHeaderMatcher(orId.getHeader());
        orChildren.addChildren(new XdsAuthorizationRule(
                new XdsAuthCondition(XdsAuthorizationType.REQUEST_HEADERS, headerName, stringMatcher)));
    }

    private static void handleRemoteIpCase(Principal orId, XdsAuthorizationRule orChildren) {
        orChildren.addChildren(new XdsAuthorizationRule(
                new XdsAuthCondition(XdsAuthorizationType.REMOTE_IP,
                        MatcherUtil.convertCidrRangeToIpMatcher(orId.getRemoteIp()))));
    }

    private static void handleDirectRemoteIpCase(Principal orId, XdsAuthorizationRule orChildren) {
        orChildren.addChildren(new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.SOURCE_IP,
                MatcherUtil.convertCidrRangeToIpMatcher(orId.getDirectRemoteIp()))));
    }

    private static void handleMetadataCase(Principal orId, XdsAuthorizationRule orChildren) {
        List<PathSegment> segments = orId.getMetadata().getPathList();
        String key = segments.get(0).getKey();

        if (REQUEST_AUTH_PRINCIPAL.equals(key)) {
            addJwtCondition(orId, orChildren, XdsAuthorizationType.REQUEST_AUTH_PRINCIPAL);
        } else if (REQUEST_AUTH_AUDIENCE.equals(key)) {
            addJwtCondition(orId, orChildren, XdsAuthorizationType.REQUEST_AUTH_AUDIENCES);
        } else if (REQUEST_AUTH_PRESENTER.equals(key)) {
            addJwtCondition(orId, orChildren, XdsAuthorizationType.REQUEST_AUTH_PRESENTERS);
        } else if (REQUEST_AUTH_CLAIMS.equals(key) && segments.size() >= SEGMENT_SIZE) {
            String matcherKey = segments.get(1).getKey();
            try {
                StringMatcher stringMatcher = MatcherUtil.convertEnvoyStringMatcher(
                        orId.getMetadata().getValue().getListMatch()
                                .getOneOf().getStringMatch());
                orChildren.addChildren(
                        new XdsAuthorizationRule(
                                new XdsAuthCondition(XdsAuthorizationType.REQUEST_AUTH_CLAIMS, matcherKey,
                                        stringMatcher)));
            } catch (Exception e) {
                LOGGER.severe("Unable to convert request auth claims: " + e.getMessage());
            }
        } else {
            LOGGER.severe("Unsupported metadate type: " + key);
        }
    }

    private static void addJwtCondition(Principal orId, XdsAuthorizationRule orChildren,
            XdsAuthorizationType authType) {
        StringMatcher stringMatcher = MatcherUtil.convertEnvoyStringMatcher(
                orId.getMetadata().getValue().getStringMatch());
        if (stringMatcher != null) {
            orChildren.addChildren(
                    new XdsAuthorizationRule(
                            new XdsAuthCondition(authType, stringMatcher)));
        }
    }

    private static XdsAuthorizationRule resolvePermission(Permission permission) {
        XdsAuthorizationRule andChildren = new XdsAuthorizationRule(ChildChainType.AND);
        for (Permission andRule : permission.getAndRules().getRulesList()) {
            if (andRule.getAny()) {
                return new XdsAuthorizationRule();
            }

            boolean isNot = false;
            if (andRule.hasNotRule()) {
                isNot = true;
                andRule = andRule.getNotRule();
            }
            Permission.Set orRules;
            XdsAuthorizationRule children;
            if (andRule.hasAndRules()) {
                orRules = andRule.getAndRules();
                children = new XdsAuthorizationRule(ChildChainType.AND, isNot);
            } else if (andRule.hasOrRules()) {
                orRules = andRule.getOrRules();
                children = new XdsAuthorizationRule(ChildChainType.OR, isNot);
            } else {
                return new XdsAuthorizationRule();
            }

            resolveChildrenRule(orRules, children, andChildren);
        }
        return andChildren;
    }

    private static void resolveChildrenRule(Permission.Set orRules, XdsAuthorizationRule children,
            XdsAuthorizationRule andChildren) {
        for (Permission orRule : orRules.getRulesList()) {
            if (orRule == null) {
                continue;
            }

            RuleCase rulecase = orRule.getRuleCase();
            switch (rulecase) {
                case DESTINATION_PORT:
                    handleDestinationPort(orRule, children);
                    break;
                case REQUESTED_SERVER_NAME:
                    handleRequestedServerName(orRule, children);
                    break;
                case DESTINATION_IP:
                    handleDestinationIp(orRule, children);
                    break;
                case URL_PATH:
                    handleUrlPath(orRule, children);
                    break;
                case HEADER:
                    handleHeader(orRule, children);
                    break;
                default:
                    break;
            }
        }

        if (!children.isEmpty()) {
            andChildren.addChildren(children);
        }
    }

    private static void handleDestinationPort(Permission orRule, XdsAuthorizationRule orChildren) {
        int port = orRule.getDestinationPort();
        if (0 != port) {
            orChildren.addChildren(
                    new XdsAuthorizationRule(
                            new XdsAuthCondition(XdsAuthorizationType.DESTINATION_PORT, new PortMatcher(port))));
        }
    }

    private static void handleRequestedServerName(Permission orRule, XdsAuthorizationRule orChildren) {
        orChildren.addChildren(
                new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.CONNECTION_SNI,
                        MatcherUtil.convertEnvoyStringMatcher(orRule.getRequestedServerName()))));
    }

    private static void handleDestinationIp(Permission orRule, XdsAuthorizationRule orChildren) {
        orChildren.addChildren(new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.DESTINATION_IP,
                MatcherUtil.convertCidrRangeToIpMatcher(orRule.getDestinationIp()))));
    }

    private static void handleUrlPath(Permission orRule, XdsAuthorizationRule orChildren) {
        StringMatcher path = MatcherUtil.convertEnvoyStringMatcher(orRule.getUrlPath().getPath());
        if (path != null) {
            orChildren.addChildren(
                    new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.URL_PATH, path)));
        }
    }

    private static void handleHeader(Permission orRule, XdsAuthorizationRule orChildren) {
        String headerName = orRule.getHeader().getName();
        StringMatcher stringMatcher = MatcherUtil.convertHeaderMatcher(orRule.getHeader());
        if (stringMatcher == null) {
            return;
        }

        if (HEADER_NAME_AUTHORITY.equals(headerName)) {
            orChildren.addChildren(
                    new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.HOSTS, stringMatcher)));
        } else if (HEADER_NAME_METHOD.equals(headerName)) {
            orChildren.addChildren(
                    new XdsAuthorizationRule(new XdsAuthCondition(XdsAuthorizationType.METHODS, stringMatcher)));
        }
    }
}
