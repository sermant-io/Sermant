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

package com.huaweicloud.intergration.registry.cloud;

import com.huaweicloud.intergration.common.utils.RequestUtils;
import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SpringCloud注册测试
 *
 * @author zhouss
 * @since 2022-11-01
 */
public class CloudRegistryTest {
    @Rule(order = 300)
    public final TestRule rule = new CloudRegistryRule();

    private final RestTemplate restTemplate = new RestTemplate();

    private final KieClient kieClient = new KieClient(restTemplate, null, getLabels());

    /**
     * 核对测试结果
     */
    @Test
    public void check() {
        test(AppType.FEIGN, 2);
        test(AppType.REST, 2);
    }

    /**
     * 测试动态关闭原注册中心
     */
    @Test
    public void testDynamicClose() throws InterruptedException {
        kieClient.publishConfig("sermant.agent.registry", "origin.__registry__.needClose: true");

        // 等待配置刷新 + 实例刷新
        Thread.sleep(42 * 1000);

        // 旧的provider将不再调用, 仅调用新注册中心的provider
        test(AppType.FEIGN, 1);
        test(AppType.REST, 1);
    }

    @After
    public void tearDown() {
        kieClient.deleteKey("sermant.agent.registry");
    }

    private void test(AppType appType, int threshold) {
        final HashSet<String> ports = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            ports.add(RequestUtils
                    .get(buildUrl(appType), Collections.emptyMap(), String.class));
        }
        Assert.assertEquals(ports.size(), threshold);
    }

    private String buildUrl(AppType appType) {
        return getAddress(appType) + "/cloudRegistry/testCloudRegistry";
    }

    private String getAddress(AppType appType) {
        if (appType == AppType.FEIGN) {
            return "http://localhost:8015";
        } else {
            return "http://localhost:8005";
        }
    }

    enum AppType {
        FEIGN,
        REST
    }

    /**
     * 获取kie订阅标签
     *
     * @return 订阅标签
     */
    protected Map<String, String> getLabels() {
        final Map<String, String> labels = new HashMap<>();
        labels.put("public", "default");
        return labels;
    }
}
