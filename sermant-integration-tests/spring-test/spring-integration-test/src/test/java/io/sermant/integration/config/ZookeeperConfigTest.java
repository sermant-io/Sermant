/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.integration.config;

import com.alibaba.fastjson.JSONObject;

import io.sermant.integration.common.utils.RequestUtils;
import io.sermant.integration.config.supprt.KieClient;
import io.sermant.integration.config.supprt.ZkClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * zookeeper动态配置生效测试
 *
 * @author zhouss
 * @since 2022-08-16
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "DYNAMIC_CONFIG_ZK")
public class ZookeeperConfigTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfigTest.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final String serverUrl = "http://127.0.0.1:8989";

    private final String key1 = "/sermant/test";
    private final String keyA = "/sermant/param1";
    private final String keyB = "/sermant/param2";
    private final String testConfigKey = "testConfig";
    private final String closeSwitchKey = "closeOriginConfigCenter";

    private KieClient kieClient;

    private ZkClient zkClient;

    /**
     * 发布zk配置
     */
    @BeforeEach
    public void publishZkConfig() {
        kieClient = new KieClient(restTemplate, null, RequestUtils.get(serverUrl + "/labels", Collections.emptyMap(),
                Map.class));
        zkClient = new ZkClient(null);
        Assertions.assertTrue(zkClient.publishConfig(key1, "1"));
        Assertions.assertTrue(zkClient.publishConfig(keyA, "a"));
        Assertions.assertTrue(zkClient.publishConfig(keyB, "b"));
    }

    /**
     * 测试启动屏蔽
     */
    @Test
    public void testBanZkConfigCenter() {
        kieClient.deleteKey(testConfigKey);
        final Boolean isOpen = get("/dynamic/config/check", Boolean.class);
        if (isOpen) {
            return;
        }
        publishKieConfig();
        check(40 * 1000, 2000, this::checkAgentConfig);
    }

    /**
     * 测试动态配置
     *
     */
    @Test
    public void testDynamicClose() {
        final Boolean isOpen = get("/dynamic/config/check", Boolean.class);
        if (!isOpen) {
            return;
        }
        // 发布动态关闭开关
        publishKieConfig();
        kieClient.publishConfig(closeSwitchKey, "sermant.origin.config.needClose: true");
        // 睡眠等待刷新， 由于LocalCse无实时通知能力，因此需要等待30S（长连接时间）,保证配置已刷新
        check(40 * 1000, 2000, this::checkAgentConfig);
    }

    private void check(long maxWaitTimeMs, long sleepTimeMs, Supplier<Boolean> checkFunc) {
        final long start = System.currentTimeMillis();
        while ((start + maxWaitTimeMs >= System.currentTimeMillis()) && !checkFunc.get()) {
            try {
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                // ignored
            }
        }
        if (!checkFunc.get()) {
            LOGGER.error("=======配置中心配置内容: [{}]==================", JSONObject.toJSONString(kieClient.query(null)));
        }
        Assertions.assertTrue(checkFunc.get());
    }

    private void publishKieConfig() {
        // 测试当前的值是否取与KIE配置中心而不是配置中心
        kieClient.publishConfig(testConfigKey, "sermant.test: 1k\nsermant"
                + ".param1: ak\nsermant.param2: bk");
    }

    private boolean checkAgentConfig() {
        final String result = restTemplate.getForObject(serverUrl + "/dynamic/config/value", String.class);
        if ("1k".equals(result)) {
            final String property = restTemplate.getForObject(serverUrl + "/dynamic/config/property", String.class);
            return "ak,bk".equals(property);
        }
        return false;
    }

    private <T> T get(String api, Class<T> responseClass) {
        return restTemplate.getForObject(serverUrl + api, responseClass);
    }

    @AfterEach
    public void close() {
        zkClient.close();
    }
}
