/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.router.spring.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * 测试类
 *
 * @author provenceee
 * @since 2022-07-26
 */
public class RouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String CONSUMER_BASE_URL = "http://127.0.0.1:8170/consumer/hello/";

    private static final String PROVIDER_URL = "http://127.0.0.1:8170/provider/hello";

    private static final int TEST_COUNT = 10;

    /**
     * 测试普通接口
     */
    @Test
    public void testFeignV1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region1", "Bar");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(CONSUMER_BASE_URL + "feign", HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testFeignV2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region", "Foo");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(CONSUMER_BASE_URL + "feign", HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testRestV1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region", "Bar");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(CONSUMER_BASE_URL + "rest", HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testRestV2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region", "foo");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(CONSUMER_BASE_URL + "rest", HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testProviderV1() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region", "Bar");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(PROVIDER_URL, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testProviderV2() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("region", "foo");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TEST_COUNT; i++) {
            exchange = REST_TEMPLATE.exchange(PROVIDER_URL, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("8162"));
        }
    }
}
