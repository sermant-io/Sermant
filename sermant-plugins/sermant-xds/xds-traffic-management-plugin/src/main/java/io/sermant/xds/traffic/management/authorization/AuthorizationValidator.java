/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on sentinel-security-core/src/main/java/com/alibaba/csp/sentinel/trust/validator/AuthValidator.java
 * from the Sentinel project.
 */

package io.sermant.xds.traffic.management.authorization;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.entity.XdsAuthCondition;
import io.sermant.core.service.xds.entity.XdsAuthorizationRule;
import io.sermant.core.service.xds.entity.XdsJwtRule;
import io.sermant.core.service.xds.entity.XdsSecurityRule;
import io.sermant.core.service.xds.entity.match.XdsAuthorizationMatcher;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.MapUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.xds.traffic.management.entity.HttpRequestEntity;

import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Authorization validator, used to validate authorization for http request and jwt token.
 *
 * @since 2025-06-14
 */
public class AuthorizationValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String UNKNOWN_IP = "unknown";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private static final String AUTHORIZATION = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthorizationValidator() {
    }

    /**
     * Validate xDS authorization rules
     *
     * @param request HttpRequestEntity
     * @param rule XdsAuthorizationRule
     * @return validate result
     */
    public static boolean validate(HttpRequestEntity request, XdsSecurityRule rule) {
        if (request == null) {
            return false;
        }
        if (rule == null) {
            return true;
        }

        Map<String, XdsJwtRule> jwtRules = rule.getJwtRules();

        if (!MapUtils.isEmpty(jwtRules)) {
            boolean hasNoToken = true;
            for (XdsJwtRule jwtRule : jwtRules.values()) {
                String token = getTokenFromJwtRule(request.getParams(), request.getHeaders(), jwtRule);
                if (!StringUtils.isEmpty(token)) {
                    hasNoToken = false;
                    Optional<JwtClaims> jwtClaims = extractJwtClaims(jwtRule.getJwks(), token);
                    if (!jwtClaims.isPresent()) {
                        return false;
                    }
                    if (!validateJwtRule(jwtRule, jwtClaims.get())) {
                        return false;
                    }
                    request.setJwtClaims(jwtClaims.get());
                    break;
                }
            }
            if (hasNoToken) {
                return false;
            }
        }

        Map<String, XdsAuthorizationRule> denyRules = rule.getDenyRules();
        for (XdsAuthorizationRule denyRule : denyRules.values()) {
            if (validateAuthRule(denyRule, request)) {
                return false;
            }
        }

        Map<String, XdsAuthorizationRule> allowRules = rule.getAllowRules();

        if (MapUtils.isEmpty(allowRules)) {
            return true;
        }

        for (XdsAuthorizationRule allowRule : allowRules.values()) {
            if (validateAuthRule(allowRule, request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate Jwt rule
     *
     * @param jwtRule XdsJwtRule
     * @param jwtClaims JwtClaims
     * @return validate result
     */
    public static boolean validateJwtRule(XdsJwtRule jwtRule, JwtClaims jwtClaims) {
        if (jwtClaims == null) {
            LOGGER.fine("JWT claims is null");
            return false;
        }

        try {
            // Validate issuer
            if (!StringUtils.isBlank(jwtRule.getIssuer())
                    && !jwtRule.getIssuer().equals(jwtClaims.getIssuer())) {
                LOGGER.fine("Issuer validation failed");
                return false;
            }

            // Validate audiences
            Set<String> acceptAud = new HashSet<>(jwtRule.getAudiences());
            if (!CollectionUtils.isEmpty(acceptAud)) {
                List<String> audiences = jwtClaims.getAudience();
                if (audiences == null
                        || audiences.stream().noneMatch(acceptAud::contains)) {
                    LOGGER.fine("Audience validation failed");
                    return false;
                }
            }

            // Validate expiration time
            if (jwtClaims.getExpirationTime() == null
                    || jwtClaims.getExpirationTime().getValueInMillis() <= System.currentTimeMillis()) {
                LOGGER.fine("Expiration time validation failed");
                return false;
            }

            return true;
        } catch (MalformedClaimException e) {
            LOGGER.log(Level.SEVERE, "Invalid JWT claims: " + jwtClaims, e);
            return false;
        }
    }

    /**
     * Get remote IP address
     *
     * @param request HttpServletRequest
     * @return remote IP address
     */
    public static String getRemoteIpAddress(HttpServletRequest request) {
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(ip) && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            int commaIndex = ip.indexOf(',');
            if (commaIndex != -1) {
                ip = ip.substring(0, commaIndex).trim();
            }
            if (!StringUtils.isEmpty(ip)) {
                return ip;
            }
        }
        if (request.getRemoteAddr() != null) {
            return request.getRemoteAddr();
        }
        return "";
    }

    /**
     * Get headers from HttpServletRequest
     *
     * @param request HttpServletRequest
     * @return Map of headers
     */
    public static Map<String, List<String>> getHeaders(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, List<String>> headers = new HashMap<>();

        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(key);
            List<String> values = new ArrayList<>();

            while (headerValues.hasMoreElements()) {
                values.add(headerValues.nextElement());
            }

            headers.put(key, Collections.unmodifiableList(values));
        }
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Get Params from HttpServletRequest
     *
     * @param request HttpServletRequest
     * @return Map of params
     */
    public static Map<String, List<String>> getParams(HttpServletRequest request) {
        if (request == null) {
            return new HashMap<>();
        }

        Map<String, List<String>> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        if (paramNames != null) {
            while (paramNames.hasMoreElements()) {
                String key = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(key);
                if (paramValues != null) {
                    List<String> values = new ArrayList<>(Arrays.asList(paramValues));
                    params.put(key, values);
                }
            }
        }
        return params;
    }

    /**
     * Get tokens from Jwt rules
     *
     * @param params params
     * @param headers headers
     * @param jwtRule XdsJwtRule
     * @return token
     */
    public static String getTokenFromJwtRule(Map<String, List<String>> params, Map<String, List<String>> headers,
            XdsJwtRule jwtRule) {
        if (jwtRule == null) {
            return StringUtils.EMPTY;
        }

        // Try to get the token from the custom header
        String token = getTokenFromCustomHeaders(headers, jwtRule);
        if (!StringUtils.isEmpty(token)) {
            return token;
        }

        // Try to get the token from the parameter
        token = getTokenFromParams(params, jwtRule);
        if (!StringUtils.isEmpty(token)) {
            return token;
        }

        // Try to get the token from the default authorization header
        token = getTokenFromDefaultHeader(headers);
        if (!StringUtils.isEmpty(token)) {
            return token;
        }

        return StringUtils.EMPTY;
    }

    private static String getTokenFromCustomHeaders(Map<String, List<String>> headers, XdsJwtRule jwtRule) {
        if (MapUtils.isEmpty(headers) || MapUtils.isEmpty(jwtRule.getFromHeaders())) {
            return StringUtils.EMPTY;
        }

        for (Map.Entry<String, String> entry : jwtRule.getFromHeaders().entrySet()) {
            String headerName = entry.getKey();
            String prefix = entry.getValue();
            List<String> auths = headers.get(headerName);

            if (!CollectionUtils.isEmpty(auths)) {
                String token = auths.get(0);
                if (token != null && token.startsWith(prefix)) {
                    return token.substring(prefix.length());
                }
            }
        }
        return StringUtils.EMPTY;
    }

    private static String getTokenFromParams(Map<String, List<String>> params, XdsJwtRule jwtRule) {
        if (MapUtils.isEmpty(params) || CollectionUtils.isEmpty(jwtRule.getFromParams())) {
            return StringUtils.EMPTY;
        }

        for (String fromParam : jwtRule.getFromParams()) {
            List<String> auths = params.get(fromParam);
            if (!CollectionUtils.isEmpty(auths)) {
                return auths.get(0);
            }
        }
        return StringUtils.EMPTY;
    }

    private static String getTokenFromDefaultHeader(Map<String, List<String>> headers) {
        if (MapUtils.isEmpty(headers)) {
            return StringUtils.EMPTY;
        }

        List<String> auths = headers.get(AUTHORIZATION);
        if (!CollectionUtils.isEmpty(auths)) {
            String token = auths.get(0);
            if (token != null && token.startsWith(BEARER_PREFIX)) {
                return token.substring(BEARER_PREFIX.length());
            }
        }
        return StringUtils.EMPTY;
    }

    private static Optional<JwtClaims> extractJwtClaims(String jwks, String token) {
        if (StringUtils.isBlank(jwks) || StringUtils.isBlank(token)) {
            return Optional.empty();
        }
        try {
            JwtConsumer jwtConsumer = buildJwtConsumer(jwks);
            JwtContext jwtContext = jwtConsumer.process(token);
            return Optional.of(jwtContext.getJwtClaims());
        } catch (JoseException e) {
            LOGGER.severe("Invalid jwks");
        } catch (InvalidJwtException e) {
            LOGGER.severe("Invalid jwt token");
        }
        return Optional.empty();
    }

    private static JwtConsumer buildJwtConsumer(String jwks) throws JoseException {
        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
                .setSkipAllValidators();
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);
        JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(
                jsonWebKeySet.getJsonWebKeys());
        jwtConsumerBuilder.setVerificationKeyResolver(jwksResolver);
        return jwtConsumerBuilder.build();
    }

    private static boolean validateAuthRule(XdsAuthorizationRule authRule, HttpRequestEntity request) {
        if (authRule.isLeaf()) {
            return validateLeafAuthRule(authRule, request);
        }
        List<XdsAuthorizationRule> ruleChildren = authRule.getChildren();
        boolean res = authRule.getChildChainType() == XdsAuthorizationRule.ChildChainType.AND;
        for (XdsAuthorizationRule ruleChild : ruleChildren) {
            boolean childRes = validateAuthRule(ruleChild, request);
            if (!childRes && authRule.getChildChainType() == XdsAuthorizationRule.ChildChainType.AND) {
                res = false;
                break;
            }
            if (childRes && authRule.getChildChainType() == XdsAuthorizationRule.ChildChainType.OR) {
                res = true;
                break;
            }
        }
        return authRule.isNot() ? !res : res;
    }

    private static boolean validateLeafAuthRule(XdsAuthorizationRule rule, HttpRequestEntity request) {
        XdsAuthCondition condition = rule.getCondition();
        XdsAuthorizationMatcher matcher = condition.getMatcher();
        String key = condition.getKey();
        if (matcher == null) {
            return false;
        }

        JwtClaims claims = request.getJwtClaims();
        try {
            switch (condition.getAuthType()) {
                case SOURCE_IP:
                    return matcher.match(request.getSourceIp());
                case REMOTE_IP:
                    return matcher.match(request.getRemoteIp());
                case DESTINATION_IP:
                    return matcher.match(request.getDestIp());
                case HOSTS:
                    return matcher.match(request.getHost());
                case METHODS:
                    return matcher.match(request.getMethod());
                case URL_PATH:
                    return matcher.match(request.getPath());
                case DESTINATION_PORT:
                    return matcher.match(request.getPort());
                case REQUEST_HEADERS:
                    return matchHeaders(matcher, key, request.getHeaders());
                case CONNECTION_SNI:
                    return matcher.match(request.getSni());
                case REQUEST_AUTH_PRINCIPAL:
                    return claims != null && matcher.match(claims.getIssuer() + "/" + claims.getSubject());
                case REQUEST_AUTH_AUDIENCES:
                    return claims != null && claims.getAudience().stream().anyMatch(matcher::match);
                case REQUEST_AUTH_PRESENTERS:
                    return claims != null && matcher.match(
                            Optional.ofNullable(claims.getClaimValueAsString("azp")).orElse(""));
                case REQUEST_AUTH_CLAIMS:
                    return claims != null && matchClaimValue(matcher, key, claims);
                default:
                    return false;
            }
        } catch (MalformedClaimException e) {
            LOGGER.warning("Parse JWT Claim failed : " + e.getMessage());
        }
        return false;
    }

    static boolean matchHeaders(XdsAuthorizationMatcher matcher, String key,
            Map<String, List<String>> headers) {
        if (headers == null || !headers.containsKey(key)) {
            return false;
        }
        List<String> headerList = headers.get(key);
        return headerList != null && headerList.stream().anyMatch(matcher::match);
    }

    static boolean matchClaimValue(XdsAuthorizationMatcher matcher, String key, JwtClaims claims)
            throws MalformedClaimException {
        Object claimValue = claims.getClaimValue(key);
        if (claimValue instanceof List) {
            return claims.getStringListClaimValue(key).stream().anyMatch(matcher::match);
        }
        return matcher.match(claims.getStringClaimValue(key));
    }
}
