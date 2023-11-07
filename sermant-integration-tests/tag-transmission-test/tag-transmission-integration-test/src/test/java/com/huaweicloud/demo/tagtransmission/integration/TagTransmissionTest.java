/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.tagtransmission.integration;

import com.huaweicloud.demo.tagtransmission.integration.utils.RequestUtils;

import com.alibaba.fastjson.JSON;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 流量标签透传插件测试
 *
 * @author daizhenyu
 * @since 2023-10-10
 **/
public class TagTransmissionTest {
    private static final Map<String, String> EXACT_TAG_MAP = new HashMap<>();

    @BeforeAll
    public static void before() {
        EXACT_TAG_MAP.put("id", "001");
    }

    /**
     * 测试httpclient3.x版本透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "HTTPCLIENTV3")
    public void testHttpClientV3() {
        checkTagTransmission("http://127.0.0.1:9048/httpClientV3/testHttpClientV3", EXACT_TAG_MAP, "httpclientv3");
    }

    /**
     * 测试httpclient4.x版本透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "HTTPCLIENTV4")
    public void testHttpClientV4() {
        checkTagTransmission("http://127.0.0.1:9049/httpClientV4/testHttpClientV4", EXACT_TAG_MAP, "httpclientv4");
    }

    /**
     * 测试okhttp透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "OKHTTP")
    public void testOkHttp() {
        checkTagTransmission("http://127.0.0.1:9055/okHttp/testOkHttp", EXACT_TAG_MAP, "okhttp");
    }

    /**
     * 测试jdkhttp和jetty服务端透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "JDKHTTP_JETTY")
    public void testJdkHttpAndJetty() {
        checkTagTransmission("http://127.0.0.1:9050/jdkHttp/testJdkHttpAndJetty", EXACT_TAG_MAP, "jdkhttp and jetty");
    }

    /**
     * 测试jdkhttp和tomcat服务端透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "JDKHTTP_TOMCAT")
    public void testJdkHttpAndTomcat() {
        checkTagTransmission("http://127.0.0.1:9050/jdkHttp/testJdkHttpAndTomcat", EXACT_TAG_MAP, "jdkhttp and tomcat");
    }

    /**
     * 测试alibaba dubbo透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "ALIBABA_DUBBO")
    public void testAlibabaDubbo() {
        checkTagTransmission("http://127.0.0.1:9041/alibabaDubbo/testAlibabaDubbo", EXACT_TAG_MAP, "alibaba dubbo");
    }

    /**
     * 测试apache dubbo透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "APACHE_DUBBO")
    public void testApacheDubbo() {
        checkTagTransmission("http://127.0.0.1:9043/apacheDubbo/testApacheDubbo", EXACT_TAG_MAP, "apache dubbo");
    }

    /**
     * 测试sofarpc透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "SOFARPC")
    public void testSofaRpc() {
        checkTagTransmission("http://127.0.0.1:9060/sofaRpc/testSofaRpc", EXACT_TAG_MAP, "sofarpc dubbo");
    }

    /**
     * 测试servicecomb rpc透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "SERVICECOMB")
    public void testServiceComb() {
        checkTagTransmission("http://127.0.0.1:9058/serviceCombConsumer/testServiceCombRpc", EXACT_TAG_MAP,
                "servicecomb rpc");
    }

    /**
     * 测试grpc透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "GRPC")
    public void testGrpc() {
        checkTagTransmission("http://127.0.0.1:9046/grpc/testGrpcByStub", EXACT_TAG_MAP, "grpc stub");
        checkTagTransmission("http://127.0.0.1:9046/grpc/testGrpcByDynamicMessage", EXACT_TAG_MAP,
                "grpc dynamic message");
    }

    /**
     * 测试跨线程透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "CROSS_THREAD")
    public void testCrossThread() {
        // 测试 跨线程透传流量标签
        Map<String, String> threadUrlMap = new HashMap<>();
        threadUrlMap.put("newThread", "http://127.0.0.1:9045/thread/testNewThread");
        threadUrlMap.put("executor", "http://127.0.0.1:9045/thread/testExecutor");
        threadUrlMap.put("submit", "http://127.0.0.1:9045/thread/testSubmit");
        threadUrlMap.put("schedule", "http://127.0.0.1:9045/thread/testSchedule");
        threadUrlMap.put("scheduleAtFixedRate", "http://127.0.0.1:9045/thread/testScheduleAtFixedRate");
        threadUrlMap.put("scheduleWithFixedDelay", "http://127.0.0.1:9045/thread/testScheduleWithFixedDelay");
        for (String key : threadUrlMap.keySet()) {
            checkTagTransmission(threadUrlMap.get(key), EXACT_TAG_MAP, key);
        }

        // 调用服务端提供的接口销毁线程池
        RequestUtils.get("http://127.0.0.1:9045/thread/shutdown", new HashMap<>());
    }

    /**
     * 测试rocketmq消息中间件透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "ROCKETMQ")
    public void testRocketmq() throws InterruptedException {
        // 发送消息之前进行一次校验，防止误用之前的流量标签
        Optional<String> checkTagOptional = RequestUtils.get("http://127.0.0.1:9056/rocketMqConsumer/queryRocketMqTag", EXACT_TAG_MAP);
        if (checkTagOptional.isPresent() && !checkTagOptional.get().equals("")) {
            Assertions.assertTrue(false, "invalid tag for rocketmq");
        }

        // 生产消息
        RequestUtils.get("http://127.0.0.1:9057/rocketMqProducer/testRocketMqProducer", EXACT_TAG_MAP);

        // sleep50秒，等待消费者消费
        Thread.sleep(50000);
        checkTagTransmission("http://127.0.0.1:9056/rocketMqConsumer/queryRocketMqTag", EXACT_TAG_MAP, "rocketmq");
    }

    /**
     * 测试kafka消息中间件透传流量标签
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "KAFKA")
    public void testKafka() throws InterruptedException {
        // 发送消息之前进行一次校验，防止误用之前的流量标签
        Optional<String> checkTagOptional = RequestUtils.get("http://127.0.0.1:9053/kafkaConsumer/queryKafkaTag", EXACT_TAG_MAP);
        if (checkTagOptional.isPresent() && !checkTagOptional.get().equals("")) {
            Assertions.assertTrue(false, "invalid tag for kafka");
        }

        // 生产消息
        RequestUtils.get("http://127.0.0.1:9054/kafkaProducer/testKafkaProducer", EXACT_TAG_MAP);

        // sleep五秒，等待消费者消费
        Thread.sleep(5000);
        checkTagTransmission("http://127.0.0.1:9053/kafkaConsumer/queryKafkaTag", EXACT_TAG_MAP, "kafka", "id");
    }

    /**
     * 测试流量标签前缀匹配、后缀匹配和动态配置，调用tomcat-demo模块的接口完成测试
     */
    @Test
    @EnabledIfSystemProperty(named = "tag.transmission.integration.test.type", matches = "CONFIG")
    public void testConfig() throws Exception {
        // 测试 流量标签前缀匹配
        Map<String, String> prefixTagMap = new HashMap<>();
        prefixTagMap.put("x-sermant-test", "tag-test-prefix");
        checkTagTransmission("http://127.0.0.1:9052/tomcat/testConfig", prefixTagMap, "prefix", "x-sermant-test");

        // 测试 流量标签后缀匹配
        Map<String, String> suffixTagMap = new HashMap<>();
        suffixTagMap.put("tag-sermant", "tag-test-suffix");
        checkTagTransmission("http://127.0.0.1:9052/tomcat/testConfig", suffixTagMap, "suffix", "tag-sermant");

        // 动态配置
        CuratorFramework curator = CuratorFrameworkFactory.newClient("127.0.0.1:2181",
                new ExponentialBackoffRetry(1000, 3));
        curator.start();
        String nodePath = "/sermant/tag-transmission-plugin/tag-config";
        String lineSeparator = System.getProperty("line.separator");
        String oldNodeValue = "enabled: true" + lineSeparator +
                "matchRule:" + lineSeparator +
                "  exact: [\"id\", \"name\"]" + lineSeparator +
                "  prefix: [\"x-sermant-\"]" + lineSeparator +
                "  suffix: [\"-sermant\"]";
        String newNodeValue = "enabled: true" + lineSeparator +
                "matchRule:" + lineSeparator +
                "  exact: [\"dynamic\", \"name\"]";

        Stat stat = curator.checkExists().forPath(nodePath);
        if (stat == null) {
            curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(nodePath,
                    newNodeValue.getBytes(StandardCharsets.UTF_8));
        } else {
            curator.setData().forPath(nodePath, newNodeValue.getBytes(StandardCharsets.UTF_8));
        }

        // sleep三秒等待动态配置生效
        Thread.sleep(3000);

        Map<String, String> dynamicTagMap = new HashMap<>();
        dynamicTagMap.put("dynamic", "tag-test-dynamic");
        Map<String, String> returnDynamicTagMap = convertJson2Map(
                RequestUtils.get("http://127.0.0.1:9052/tomcat/testConfig", dynamicTagMap));

        // 切换为原来的流量标签配置
        curator.setData().forPath(nodePath, oldNodeValue.getBytes(StandardCharsets.UTF_8));
        curator.close();
        Assertions.assertEquals("tag-test-dynamic", returnDynamicTagMap.get("dynamic"),
                "dynamic config transmit traffic tag failed");
    }

    private void checkTagTransmission(String url, Map<String, String> tagMap, String message, String tagKey) {
        Map<String, String> returnTagMap = convertJson2Map(RequestUtils.get(url,
                tagMap));
        Assertions.assertEquals(tagMap.get(tagKey), returnTagMap.get(tagKey), message + " transmit traffic tag failed");
    }

    private void checkTagTransmission(String url, Map<String, String> tagMap, String message) {
        checkTagTransmission(url, tagMap, message, "id");
    }

    private Map<String, String> convertJson2Map(Optional<String> jsonOptional) {
        if (jsonOptional.isPresent()) {
            return JSON.parseObject(jsonOptional.get(), Map.class);
        }
        return new HashMap<>();
    }
}