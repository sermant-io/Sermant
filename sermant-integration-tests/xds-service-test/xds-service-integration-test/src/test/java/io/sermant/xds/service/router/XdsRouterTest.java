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

package io.sermant.xds.service.router;

import io.sermant.xds.service.utils.HttpRequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * xDS router test
 *
 * @author daizhenyu
 * @since 2024-10-08
 **/
public class XdsRouterTest {
    /**
     * test xds router with header and path
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "ROUTER_HEADER_PATH")
    public void testRouterWithHeaderAndPath() {
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/okHttp2?host=spring-server&version=v1"));
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/httpAsyncClient?host=spring-server&version=v1"));
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/httpClient?host=spring-server&version=v1"));
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/jdkHttp?host=spring-server&version=v1"));
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/okHttp3?host=spring-server&version=v1"));
        Assertions.assertEquals("v1",
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/restTemplate?host=spring-server&version=v1"));
    }

    /**
     * test xds router with random lb
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "LB_RANDOM")
    public void testRouterWithRandom() {
        int[][] results = new int[5][2];

        int count = 100;
        while (count > 0) {
            count--;
            countCalls(results, 0,
                    HttpRequestUtils.doGet("http://127.0.0.1:8080/router/okHttp2?host=spring-server&version=v2"));
            countCalls(results, 1,
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/httpClient?host=spring-server&version=v2"));
            countCalls(results, 2,
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/jdkHttp?host=spring-server&version=v2"));
            countCalls(results, 3,
                    HttpRequestUtils.doGet("http://127.0.0.1:8080/router/httpAsyncClient?host=spring-server&version"
                            + "=v2"));
            countCalls(results, 4,
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/okHttp3?host=spring-server&version=v2"));
        }
        Assertions.assertTrue(isRandom(results, 0), "okHttp2 random lb policy does not take effect");
        Assertions.assertTrue(isRandom(results, 1), "httpClient random lb policy does not take effect");
        Assertions.assertTrue(isRandom(results, 2), "jdkHttp random lb policy does not take effect");
        Assertions.assertTrue(isRandom(results, 3), "httpAsyncClient random lb policy does not take effect");
        Assertions.assertTrue(isRandom(results, 4), "okHttp3 random lb policy does not take effect");
    }

    /**
     * test xds router with round-robin lb
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "LB_ROUND_ROBIN")
    public void testRouterWithRoundRobin() {
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/okHttp2?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/okHttp2?host=spring-server&version=v2"));
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/httpClient?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/httpClient?host=spring-server&version=v2"));
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/jdkHttp?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/jdkHttp?host=spring-server&version=v2"));
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/httpAsyncClient?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8080/router/httpAsyncClient?host=spring-server&version=v2"));
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/okHttp3?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/okHttp3?host=spring-server&version=v2"));
        Assertions.assertNotEquals(
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/restTemplate?host=spring-server&version=v2"),
                HttpRequestUtils.doGet("http://127.0.0.1:8082/router/restTemplate?host=spring-server&version=v2"));
    }

    /**
     * test xds router with weighted cluster
     */
    @Test
    @EnabledIfSystemProperty(named = "xds.service.integration.test.type", matches = "ROUTER_WEIGHT")
    public void testRouterWithWeight() {
        int count = 3;
        while (count > 0) {
            count--;
            Assertions.assertEquals("v2",
                    HttpRequestUtils.doGet("http://127.0.0.1:8080/router/okHttp2?host=spring-server&version=base"));
            Assertions.assertEquals("v2",
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/httpClient?host=spring-server&version=base"));
            Assertions.assertEquals("v2",
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/jdkHttp?host=spring-server&version=base"));
            Assertions.assertEquals("v2",
                    HttpRequestUtils
                            .doGet("http://127.0.0.1:8080/router/httpAsyncClient?host=spring-server&version=base"));
            Assertions.assertEquals("v2",
                    HttpRequestUtils.doGet("http://127.0.0.1:8082/router/okHttp3?host=spring-server&version=base"));
            Assertions.assertEquals("v2",
                    HttpRequestUtils
                            .doGet("http://127.0.0.1:8082/router/restTemplate?host=spring-server&version=base"));
        }
    }

    private void countCalls(int[][] results, int index, String result) {
        switch (result) {
            case "v1":
                results[index][0]++;
                break;
            case "v2":
                results[index][1]++;
                break;
        }
    }

    private boolean isRandom(int[][] results, int index) {
        int differenceValue = Math.abs(results[index][0] - results[index][1]);
        if (differenceValue <= 30) {
            return true;
        }
        return false;
    }
}
