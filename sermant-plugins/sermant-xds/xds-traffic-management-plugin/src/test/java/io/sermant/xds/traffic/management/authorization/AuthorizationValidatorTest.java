/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.traffic.management.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import io.sermant.core.service.xds.entity.XdsJwtRule;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationValidatorTest {
    @Mock
    private XdsJwtRule jwtRule;

    @Mock
    private JwtClaims jwtClaims;

    @Mock
    private NumericDate expirationTime;

    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() {

    }

    /**
     * Test validate JwtRule with null jwtClaims.
     */
    @Test
    public void testValidateJwtRuleWithNullJwtClaims() {
        assertFalse(AuthorizationValidator.validateJwtRule(jwtRule, null));
    }

    /**
     * Test validate JwtRule with issuer not match.
     */
    @Test
    public void testValidateJwtRuleWithIssuerNotMatch() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer2");

        assertFalse(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with issuer match.
     */
    @Test
    public void testValidateJwtRuleWithIssuerMatch() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud1"));
        when(jwtClaims.getExpirationTime()).thenReturn(expirationTime);
        when(expirationTime.getValueInMillis()).thenReturn(System.currentTimeMillis() + 10000L);

        assertTrue(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with empty audiences.
     */
    @Test
    public void testValidateJwtRuleWithEmptyAudiences() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Collections.emptyList());
        when(jwtClaims.getExpirationTime()).thenReturn(expirationTime);
        when(expirationTime.getValueInMillis()).thenReturn(System.currentTimeMillis() + 10000L);

        assertTrue(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with no matching audience
     */
    @Test
    public void testValidateJwtRuleWithNoMatchingAudience() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud3"));
        assertFalse(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with matching audience
     */
    @Test
    public void testValidateJwtRuleWithMatchingAudience() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud1"));
        when(jwtClaims.getExpirationTime()).thenReturn(expirationTime);
        when(expirationTime.getValueInMillis()).thenReturn(System.currentTimeMillis() + 10000L);

        assertTrue(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validateJwtRule with null expiration time
     */
    @Test
    public void testValidateJwtRuleWithNullExpirationTime() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud1"));
        when(jwtClaims.getExpirationTime()).thenReturn(null);

        assertFalse(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with expired token
     */
    @Test
    public void testValidateJwtRuleWithExpiredToken() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud1"));
        when(jwtClaims.getExpirationTime()).thenReturn(expirationTime);
        when(expirationTime.getValueInMillis()).thenReturn(System.currentTimeMillis() - 10000L);

        assertFalse(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test validate JwtRule with valid token.
     */
    @Test
    public void testValidateJwtRuleWithValidToken() throws MalformedClaimException {
        when(jwtRule.getIssuer()).thenReturn("issuer1");
        when(jwtClaims.getIssuer()).thenReturn("issuer1");
        when(jwtRule.getAudiences()).thenReturn(Arrays.asList("aud1", "aud2"));
        when(jwtClaims.getAudience()).thenReturn(Collections.singletonList("aud1"));
        when(jwtClaims.getExpirationTime()).thenReturn(expirationTime);
        when(expirationTime.getValueInMillis()).thenReturn(System.currentTimeMillis() + 10000L);

        assertTrue(AuthorizationValidator.validateJwtRule(jwtRule, jwtClaims));
    }

    /**
     * Test if the X-Forwarded-For header is present and is a single IP
     */
    @Test
    public void testGetRemoteIpAddressWithSingleXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("192.168.1.1", result);
    }

    /**
     * Test if the X-Forwarded-For header exists and is multiple IP addresses (take the first one)
     */
    @Test
    public void testGetRemoteIpAddressWithMultipleXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 192.168.1.2, 192.168.1.3");

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("192.168.1.1", result);
    }

    /**
     * Test for the case where the X-Forwarded-For header exists but the value is "unknown".
     */
    @Test
    public void testGetRemoteIpAddressWithUnknownXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("192.168.1.100", result);
    }

    /**
     * To test if the X-Forwarded-For header does not exist, use getRemoteAddr
     */
    @Test
    public void testGetRemoteIpAddressWithoutXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("192.168.1.100", result);
    }

    /**
     * If the IP address cannot be obtained by all methods, an empty string is returned
     */
    @Test
    public void testGetRemoteIpAddressWhenAllMethodsFail() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("", result);
    }

    /**
     * Test the case where the X-Forwarded-For header exists but is an empty string
     */
    @Test
    public void testGetRemoteIpAddressWithEmptyXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        String result = AuthorizationValidator.getRemoteIpAddress(request);
        assertEquals("192.168.1.100", result);
    }

    /**
     * Test get headers with null request
     */
    @Test
    public void testGetHeadersWithNullRequest() {
        Map<String, List<String>> result = AuthorizationValidator.getHeaders(null);
        assertTrue(result.isEmpty());
    }

    /**
     * Test get headers with no headers
     */
    @Test
    public void testGetHeadersWithNoHeaders() {
        Enumeration<String> emptyHeaderNames = Collections.emptyEnumeration();
        when(request.getHeaderNames()).thenReturn(emptyHeaderNames);

        Map<String, List<String>> result = AuthorizationValidator.getHeaders(request);
        assertTrue(result.isEmpty());
    }

    /**
     * Test get headers with single header single value
     */
    @Test
    public void testGetHeadersWithSingleHeaderSingleValue() {
        Enumeration<String> headerNames = Collections.enumeration(Collections.singletonList("Content-Type"));
        when(request.getHeaderNames()).thenReturn(headerNames);

        Enumeration<String> headerValues = Collections.enumeration(Collections.singletonList("application/json"));
        when(request.getHeaders("Content-Type")).thenReturn(headerValues);

        Map<String, List<String>> result = AuthorizationValidator.getHeaders(request);

        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("application/json"), result.get("Content-Type"));
    }

    /**
     * Test get headers with single header multiple values
     */
    @Test
    public void testGetHeadersWithSingleHeaderMultipleValues() {
        Enumeration<String> headerNames = Collections.enumeration(Collections.singletonList("Accept"));
        when(request.getHeaderNames()).thenReturn(headerNames);

        List<String> values = Arrays.asList("text/html", "application/xml");
        Enumeration<String> headerValues = Collections.enumeration(values);
        when(request.getHeaders("Accept")).thenReturn(headerValues);

        Map<String, List<String>> result = AuthorizationValidator.getHeaders(request);

        assertEquals(1, result.size());
        assertEquals(values, result.get("Accept"));
    }

    /**
     * Test get headers with multiple headers
     */
    @Test
    public void testGetHeadersWithMultipleHeaders() {
        List<String> headerNameList = Arrays.asList("Content-Type", "Accept", "User-Agent");
        Enumeration<String> headerNames = Collections.enumeration(headerNameList);
        when(request.getHeaderNames()).thenReturn(headerNames);

        Enumeration<String> contentTypeValues = Collections.enumeration(Collections.singletonList("application/json"));
        when(request.getHeaders("Content-Type")).thenReturn(contentTypeValues);

        List<String> acceptValues = Arrays.asList("text/html", "application/xml");
        Enumeration<String> acceptValueEnum = Collections.enumeration(acceptValues);
        when(request.getHeaders("Accept")).thenReturn(acceptValueEnum);

        Enumeration<String> userAgentValues = Collections.enumeration(Collections.singletonList("Mozilla/5.0"));
        when(request.getHeaders("User-Agent")).thenReturn(userAgentValues);

        Map<String, List<String>> result = AuthorizationValidator.getHeaders(request);

        assertEquals(3, result.size());
        assertEquals(Collections.singletonList("application/json"), result.get("Content-Type"));
        assertEquals(acceptValues, result.get("Accept"));
        assertEquals(Collections.singletonList("Mozilla/5.0"), result.get("User-Agent"));
    }

    /**
     * 测试null请求情况
     */
    @Test
    public void testGetParamsWithNullRequest() {
        Map<String, List<String>> result = AuthorizationValidator.getParams(null);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试无参数请求情况
     */
    @Test
    public void testGetParamsWithNoParameters() {
        // 模拟无参数情况
        when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

        Map<String, List<String>> result = AuthorizationValidator.getParams(request);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试单参数单值情况
     */
    @Test
    public void testGetParamsWithSingleParameterSingleValue() {
        // 模拟参数名枚举
        Vector<String> paramNames = new Vector<>();
        paramNames.add("param1");
        Enumeration<String> enumeration = paramNames.elements();

        // 模拟请求行为
        when(request.getParameterNames()).thenReturn(enumeration);
        when(request.getParameterValues("param1")).thenReturn(new String[]{"value1"});

        Map<String, List<String>> result = AuthorizationValidator.getParams(request);

        assertEquals(1, result.size());
        assertEquals(Collections.singletonList("value1"), result.get("param1"));
    }

    /**
     * 测试单参数多值情况
     */
    @Test
    public void testGetParamsWithSingleParameterMultipleValues() {
        // 模拟参数名枚举
        Vector<String> paramNames = new Vector<>();
        paramNames.add("param1");
        Enumeration<String> enumeration = paramNames.elements();

        // 模拟请求行为
        when(request.getParameterNames()).thenReturn(enumeration);
        when(request.getParameterValues("param1")).thenReturn(new String[]{"value1", "value2"});

        Map<String, List<String>> result = AuthorizationValidator.getParams(request);

        assertEquals(1, result.size());
        assertEquals(Arrays.asList("value1", "value2"), result.get("param1"));
    }

    /**
     * 测试多参数情况
     */
    @Test
    public void testGetParamsWithMultipleParameters() {
        // 模拟参数名枚举
        Vector<String> paramNames = new Vector<>();
        paramNames.add("param1");
        paramNames.add("param2");
        Enumeration<String> enumeration = paramNames.elements();

        // 模拟请求行为
        when(request.getParameterNames()).thenReturn(enumeration);
        when(request.getParameterValues("param1")).thenReturn(new String[]{"value1"});
        when(request.getParameterValues("param2")).thenReturn(new String[]{"value2", "value3"});

        Map<String, List<String>> result = AuthorizationValidator.getParams(request);

        assertEquals(2, result.size());
        assertEquals(Collections.singletonList("value1"), result.get("param1"));
        assertEquals(Arrays.asList("value2", "value3"), result.get("param2"));
    }

    /**
     * 测试参数值为null的情况
     */
    @Test
    public void testGetParamsWithNullParameterValues() {
        // 模拟参数名枚举
        Vector<String> paramNames = new Vector<>();
        paramNames.add("param1");
        Enumeration<String> enumeration = paramNames.elements();

        // 模拟请求行为
        when(request.getParameterNames()).thenReturn(enumeration);
        when(request.getParameterValues("param1")).thenReturn(null);

        Map<String, List<String>> result = AuthorizationValidator.getParams(request);

        assertEquals(0, result.size());
        assertNull(result.get("param1"));
    }
}
