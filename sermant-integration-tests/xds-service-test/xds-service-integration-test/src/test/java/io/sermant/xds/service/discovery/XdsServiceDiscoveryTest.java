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

package io.sermant.xds.service.discovery;

import io.sermant.xds.service.utils.HttpRequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * xDS service discovery test
 *
 * @author daizhenyu
 * @since 2024-07-15
 **/
public class XdsServiceDiscoveryTest {
    /**
     * one server instance
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "DISCOVERY_ONE_SERVER_INSTANCE")
    public void testDiscoveryWithOneServerInstance() {
        String[] resultStrings = splitResult(HttpRequestUtils.doGet("http://127.0.0.1:8080/hello"));
        Assertions.assertEquals(2, resultStrings.length, "The returned result format is incorrect.");
        Assertions.assertEquals("hello", resultStrings[0]);
        Assertions.assertEquals("1", resultStrings[1]);
    }

    /**
     * zero server instance
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "DISCOVERY_ZERO_SERVER_INSTANCE")
    public void testDiscoveryWithZeroServerInstance() {
        String[] resultStrings = splitResult(HttpRequestUtils.doGet("http://127.0.0.1:8080/hello"));
        Assertions.assertEquals(2, resultStrings.length, "The returned result format is incorrect.");
        Assertions.assertEquals("0", resultStrings[1]);
    }

    /**
     * two server instance
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "DISCOVERY_TWO_SERVER_INSTANCE")
    public void testDiscoveryWithTwoServerInstance() {
        String[] resultStrings = splitResult(HttpRequestUtils.doGet("http://127.0.0.1:8080/hello"));
        Assertions.assertEquals(2, resultStrings.length, "The returned result format is incorrect.");
        Assertions.assertEquals("hello", resultStrings[0]);
        Assertions.assertEquals("2", resultStrings[1]);
    }

    /**
     * secret
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "SECRET")
    public void testDiscoveryWithSecret() {
        String[] resultStrings = splitResult(HttpRequestUtils.doGet("http://127.0.0.1:8080/hello"));
        Assertions.assertEquals(2, resultStrings.length, "The returned result format is incorrect.");
        Assertions.assertEquals("hello", resultStrings[0]);
        Assertions.assertEquals("2", resultStrings[1]);
    }

    /**
     * subscribe to get service instance
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "DISCOVERY_SUBSCRIBE")
    public void testDiscoveryWithSubscribe() {
        String[] resultStrings = splitResult(HttpRequestUtils.doGet("http://127.0.0.1:8080/hello"));
        Assertions.assertEquals(2, resultStrings.length, "The returned result format is incorrect.");
        Assertions.assertEquals("hello", resultStrings[0]);
        Assertions.assertEquals("2", resultStrings[1]);
    }

    /**
     * one server instance with client using envoy
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches =
            "DISCOVERY_ONE_SERVER_INSTANCE_ENVOY")
    public void testDiscoveryWithClientUsingEnvoyAndOneInstance() {
        Assertions.assertEquals("hello", HttpRequestUtils.doGet("http://127.0.0.1:8080/hello?address=spring-server"
                + ".default.svc.cluster.local:8080"));
    }

    /**
     * zero server instance with client using envoy
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches =
            "DISCOVERY_ZERO_SERVER_INSTANCE_ENVOY")
    public void testDiscoveryWithClientUsingEnvoyAndZeroInstance() {
        Assertions.assertEquals("", HttpRequestUtils.doGet("http://127.0.0.1:8080/hello?address=spring-server"
                + ".default.svc.cluster.local:8080"));
    }

    private String[] splitResult(String result) {
        Assertions.assertNotNull(result, "The returned result is null.");
        return result.split("-");
    }
}
