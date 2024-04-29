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

package io.sermant.integration.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * 测试spring dubbo双注册场景
 *
 * @author provenceee
 * @since 2022-05-05
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "common")
public class SpringAndDubboTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final String baseUrl;

    /**
     * 构造方法
     */
    public SpringAndDubboTest() {
        baseUrl = "http://127.0.0.1:" + System.getProperty("controller.port", "28020") + "/consumer/";
    }

    /**
     * 测试普通接口
     */
    @Test
    public void testCommonInterface() {
        Assertions
            .assertEquals("foo:Foo", REST_TEMPLATE.getForObject(baseUrl + "testFoo?str=Foo", String.class));
        Assertions.assertEquals("foo2:Foo2",
            REST_TEMPLATE.getForObject(baseUrl + "testFoo2?str=Foo2", String.class));
    }

    /**
     * 测试通配符接口
     */
    @Test
    public void testWildcardInterface() {
        Assertions.assertEquals("foo:Foo",
            REST_TEMPLATE.getForObject(baseUrl + "wildcard/testFoo?str=Foo", String.class));
        Assertions.assertEquals("foo2:Foo2",
            REST_TEMPLATE.getForObject(baseUrl + "wildcard/testFoo2?str=Foo2", String.class));
    }

    /**
     * 测试有多个实现的接口
     */
    @Test
    public void testMultipleImplementation() {
        Assertions.assertEquals("bar1:Bar1",
            REST_TEMPLATE.getForObject(baseUrl + "testBar?str=Bar1", String.class));
        Assertions.assertEquals("bar2:Bar2",
            REST_TEMPLATE.getForObject(baseUrl + "testBar2?str=Bar2", String.class));
    }

    /**
     * 测试泛化接口
     */
    @Test
    public void testGenericService() {
        Assertions.assertEquals("bar1:BAR1",
            REST_TEMPLATE.getForObject(baseUrl + "testBarGeneric?str=BAR1", String.class));
    }

    /**
     * 测试tag标签路由
     */
    @Test
    public void testTag() {
        Assertions.assertEquals("foo2:app1",
            REST_TEMPLATE.getForObject(baseUrl + "testTag?tag=app1", String.class));
        Assertions.assertThrows(HttpServerErrorException.class,
            () -> REST_TEMPLATE.getForObject(baseUrl + "testTag?tag=app2", String.class));
    }

    /**
     * 测试feign
     */
    @Test
    public void testFeign() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i <= 10; i++) {
            set.add(REST_TEMPLATE.getForObject(baseUrl + "hello/feign", String.class));
            if (set.size() >= 2) {
                break;
            }
        }
        Assertions.assertTrue(set.size() >= 2);
    }

    /**
     * 测试restTemplate
     */
    @Test
    public void testRestTemplate() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i <= 10; i++) {
            set.add(REST_TEMPLATE.getForObject(baseUrl + "hello/rest", String.class));
            if (set.size() >= 2) {
                break;
            }
        }
        Assertions.assertTrue(set.size() >= 2);
    }
}