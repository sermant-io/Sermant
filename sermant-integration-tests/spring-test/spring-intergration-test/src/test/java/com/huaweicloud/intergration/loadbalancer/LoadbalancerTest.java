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

package com.huaweicloud.intergration.loadbalancer;

import com.huaweicloud.intergration.common.LoadbalancerConstants;
import com.huaweicloud.intergration.common.rule.DisableRule;
import com.huaweicloud.intergration.common.utils.EnvUtils;
import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡测试
 *
 * @author zhouss
 * @since 2022-08-17
 */
public class LoadbalancerTest {
    @Rule
    public final TestRule TEST_RULE = new DisableRule();

    private static final String RELEASE_FLAG = "RELEASE";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalancerTest.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final KieClient kieClient = new KieClient(restTemplate, null, getLabels());

    private final String lbKey = getLbKey();
    private final String lbMatchGroup = getLbMatchGroup();

    private final Map<String, String> ribbonLbRules = new HashMap<>();
    private final Map<String, String> springLbRules = new HashMap<>();


    /**
     * spring boot版本号
     */
    private String springBootVersion;

    @Before
    public void ready() {
        initRibbon();
        initSpring();
        final String property = EnvUtils.getEnv(LoadbalancerConstants.SPRING_BOOT_VERSION_ENV_KEY, null);
        Assert.assertNotNull(property);
        this.springBootVersion = property;
    }

    private void initRibbon() {
        ribbonLbRules.put("Random", "com.netflix.loadbalancer.RandomRule");
        ribbonLbRules.put("RoundRobin", "com.netflix.loadbalancer.RoundRobinRule");
        ribbonLbRules.put("Retry", "com.netflix.loadbalancer.RetryRule");
        ribbonLbRules.put("BestAvailable", "com.netflix.loadbalancer.BestAvailableRule");
        ribbonLbRules.put("AvailabilityFiltering", "com.netflix.loadbalancer.AvailabilityFilteringRule");
        ribbonLbRules.put("ResponseTimeWeighted", "com.netflix.loadbalancer.ResponseTimeWeightedRule");
        ribbonLbRules.put("ZoneAvoidance", "com.netflix.loadbalancer.ZoneAvoidanceRule");
        ribbonLbRules.put("WeightedResponseTime", "com.netflix.loadbalancer.WeightedResponseTimeRule");
    }

    private void initSpring() {
        springLbRules.put("Random", "org.springframework.cloud.loadbalancer.core.RandomLoadBalancer");
        springLbRules.put("RoundRobin", "org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer");
    }

    @Test
    public void test() {
        kieClient.deleteKey(lbKey);
        kieClient.deleteKey(lbMatchGroup);
        if (isValidForRibbon()) {
            testRandomRule("getRibbonLb", ribbonLbRules);
        } else {
            testRandomRule("getSpringLb", springLbRules);
        }
    }

    private void testRandomRule(String api, Map<String, String> rules) {
        // 测试存在服务名场景
        final AtomicInteger index = new AtomicInteger(-1);
        String rule = rollRule(rules, index);
        LOGGER.info("===============Test rule {}==============", rule);
        publishLoadbalancerRule(rule, getServiceName());
        requestWithCycle(api, "?serviceName=" + getServiceName(), 30 * 1000, 2000,
                rules.get(rule));
        // 测试服务名为空，且发布新的规则
        rule = rollRule(rules, index);
        LOGGER.info("===============Test rule {}==============", rule);
        publishLoadbalancerRule(rule, null);
        requestWithCycle(api, "?serviceName=" + getServiceName(), 30 * 1000, 2000,
                rules.get(rule));
    }

    private String rollRule(Map<String, String> rules, AtomicInteger rolledIndex) {
        int random = rules.size();
        int index = getIndex(random, rolledIndex);
        rolledIndex.set(index);
        final Set<Entry<String, String>> entries = rules.entrySet();
        for (Entry<String, String> entry : entries) {
            if (index-- != 0) {
                continue;
            }
            return entry.getKey();
        }
        return "Random";
    }

    private int getIndex(int max, AtomicInteger rolledIndex) {
        int index = new Random().nextInt(max);
        if (index != rolledIndex.get()) {
            return index;
        }
        return getIndex(max, rolledIndex);
    }

    private void requestWithCycle(String api, String param, long maxTimeMs, long sleepTime, String expected) {
        final long start = System.currentTimeMillis();
        String result = null;
        while ((start + maxTimeMs >= System.currentTimeMillis()) && !expected.equals(result)) {
            result = request(api, param);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // ignored
            }
        }
        Assert.assertEquals(result, expected);
    }

    private String request(String api, String param) {
        return restTemplate
                .getForObject(getUrl() + "/" + api + (param == null ? "" : param), String.class);
    }

    private void publishLoadbalancerRule(String rule, String serviceName) {
        kieClient.publishConfig(lbKey, getLoadbalancer(rule));
        kieClient.publishConfig(lbMatchGroup, getMatchGroup(serviceName));
    }

    private static String getLoadbalancer(String rule) {
        return "rule: " + rule;
    }

    private static String getMatchGroup(String serviceName) {
        return "alias: flowcontrol111\n"
                + "matches:\n"
                + "- apiPath:\n"
                + "    exact: /sc/provider/\n"
                + "  headers: {}\n"
                + "  method:\n"
                + "  - GET\n"
                + "  name: degrade\n"
                + "  showAlert: false\n"
                + "  uniqIndex: c3w7x\n"
                + ((serviceName != null) ? ("  serviceName: " + serviceName + "\n") : "");
    }

    /**
     * 根据版本判断当前是否适合使用ribbon测试
     *
     * @return 是否适合
     */
    private boolean isValidForRibbon() {
        String formatVersion = springBootVersion;
        if (springBootVersion.contains(RELEASE_FLAG)) {
            formatVersion = springBootVersion.substring(0, springBootVersion.lastIndexOf('.'));
        }
        final String[] split = formatVersion.split("\\.");
        if (Integer.parseInt(split[0]) > 1 && Integer.parseInt(split[1]) >= 4) {
            // 表示当版本大于2.4.0版本时, 不再适用ribbon负载均衡
            return false;
        }
        return true;
    }

    /**
     * 生效服务名
     *
     * @return 服务名, 指消费端
     */
    protected String getServiceName() {
        return "rest-provider";
    }

    /**
     * 获取请求地址
     *
     * @return 接口地址
     */
    protected String getUrl() {
        return "http://localhost:8005/lb";
    }

    /**
     * 获取负载均衡key
     *
     * @return key
     */
    protected String getLbKey() {
        return "servicecomb.loadbalance.test";
    }

    /**
     * 获取负载均衡group
     *
     * @return group
     */
    protected String getLbMatchGroup() {
        return "servicecomb.matchGroup.test";
    }

    /**
     * 获取kie订阅标签
     *
     * @return 订阅标签
     */
    protected Map<String, String> getLabels() {
        return null;
    }
}
