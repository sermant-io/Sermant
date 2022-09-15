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

package com.huaweicloud.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 测试dubbo接口
 *
 * @author provenceee
 * @since 2022-05-05
 */
public class ConsumerTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String BASE_URL = "http://127.0.0.1:28820/consumer/";

    /**
     * 测试普通接口
     */
    @Test
    public void testCommonInterface() {
        Assertions.assertEquals("foo:Foo", REST_TEMPLATE.getForObject(BASE_URL + "testFoo?str=Foo", String.class));
        Assertions.assertEquals("foo2:Foo2", REST_TEMPLATE.getForObject(BASE_URL + "testFoo2?str=Foo2", String.class));
    }

    /**
     * 测试通配符接口
     */
    @Test
    public void testWildcardInterface() {
        Assertions.assertEquals("foo:Foo",
            REST_TEMPLATE.getForObject(BASE_URL + "wildcard/testFoo?str=Foo", String.class));
        Assertions.assertEquals("foo2:Foo2",
            REST_TEMPLATE.getForObject(BASE_URL + "wildcard/testFoo2?str=Foo2", String.class));
    }

    /**
     * 测试有多个实现的接口
     */
    @Test
    public void testMultipleImplementation() {
        Assertions.assertEquals("bar1:Bar1", REST_TEMPLATE.getForObject(BASE_URL + "testBar?str=Bar1", String.class));
        Assertions.assertEquals("bar2:Bar2", REST_TEMPLATE.getForObject(BASE_URL + "testBar2?str=Bar2", String.class));
    }

    /**
     * 测试泛化接口
     */
    @Test
    public void testGenericService() {
        Assertions
            .assertEquals("bar1:BAR1", REST_TEMPLATE.getForObject(BASE_URL + "testBarGeneric?str=BAR1", String.class));
    }

    /**
     * 测试tag标签路由
     */
    @Test
    public void testTag() {
        Assertions.assertEquals("foo2:app1", REST_TEMPLATE.getForObject(BASE_URL + "testTag?tag=app1", String.class));
        Assertions.assertThrows(HttpServerErrorException.class,
            () -> REST_TEMPLATE.getForObject(BASE_URL + "testTag?tag=app2", String.class));
    }

    /**
     * 测试多注册中心
     */
    @Test
    public void testMultipleRegistry() throws InterruptedException {
        // 因为多注册中心是随机选择一个注册中心的节点进行访问，所以这里访问100次，这个用例失败并不一定真的失败，需要详细分析
        Set<String> set = new HashSet<>();
        for (int i = 0; i <= 100; i++) {
            set.add(REST_TEMPLATE.getForObject(BASE_URL + "getRegistryProtocol", String.class));
            if (set.size() >= 2) {
                break;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        Assertions.assertTrue(set.size() >= 2);
    }
}