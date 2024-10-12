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

package io.sermant.router.dubbo.service;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.cache.DubboCache;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.service.AbstractDirectoryService;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.config.cache.ConfigCache;
import io.sermant.router.dubbo.ApacheInvoker;
import io.sermant.router.dubbo.RuleInitializationUtils;

import org.apache.dubbo.common.utils.MapUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test AbstractDirectoryService
 *
 * @author provenceee
 * @since 2022-09-14
 */
public class AbstractDirectoryServiceTest {
    private static AbstractDirectoryService service;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static RouterConfig config;

    /**
     * Mock before UT execution
     */
    @BeforeClass
    public static void before() {
        config = new RouterConfig();
        config.setZone("foo");
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(config);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(new TransmitConfig());

        service = new AbstractDirectoryServiceImpl();
    }

    /**
     * Release mock objects after UT execution
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
        DubboCache.INSTANCE.clear();
    }

    public AbstractDirectoryServiceTest() {
    }

    @Before
    public void clear() {
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "");
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        config.setUseRequestRouter(false);
    }

    /**
     * when the test is invalid
     */
    @Test
    public void testSelectInvokersWhenInvalid() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        TestObject testObject = new TestObject();
        Invocation invocation = new ApacheInvocation();

        // Test arguments as null
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, null, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // Test arguments are empty
        targetInvokers = (List<Object>) service.selectInvokers(testObject, new Object[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // Set arguments
        Object[] arguments = {invocation};

        // Initialize routing rules
        RuleInitializationUtils.initFlowMatchRule();

        // Test passing empty attachment and queryMap
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("foo", Collections.singletonList("foo1")));
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());
        Assert.assertEquals("foo1", invocation.getAttachment("foo"));
        ThreadLocalUtils.removeRequestTag();

        // Side is not a consumer
        testObject.getQueryMap().put("side", "");
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // TargetService is empty
        testObject.getQueryMap().put("side", "consumer");
        testObject.getQueryMap().put("interface", "io.sermant.foo.FooTest");
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // the test routing rule is invalid
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "foo");
        testObject.getQueryMap().put("side", "consumer");
        testObject.getQueryMap().put("interface", "io.sermant.foo.FooTest");
        targetInvokers = (List<Object>) service
                .selectInvokers(testObject, arguments[0], Collections.singletonList(invoker1));
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));

        // the rule is invalid
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "foo");
        testObject.getQueryMap().put("side", "consumer");
        testObject.getQueryMap().put("interface", "io.sermant.foo.FooTest");
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * Test the getGetTargetInvokers method (flow matching rule)
     */
    @Test
    public void testGetTargetInvokersByFlowRules() {
        // initialize the routing rule
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        TestObject testObject = new TestObject();
        Invocation invocation = new ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Object[] arguments = new Object[]{invocation};
        Map<String, String> queryMap = testObject.getQueryMap();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "io.sermant.foo.FooTest");
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * Test the getGetTargetExamples method (tag matching rule)
     */
    @Test
    public void testGetTargetInvokerByTagRules() {
        // initialize the routing rule
        RuleInitializationUtils.initTagMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        TestObject testObject = new TestObject();
        Invocation invocation = new ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Object[] arguments = new Object[]{invocation};
        Map<String, String> queryMap = testObject.getQueryMap();
        queryMap.put("side", "consumer");
        queryMap.put("group", "red");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "io.sermant.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * Test the getGetTargetExamples method (flow matching rule and tag matching rule)
     */
    @Test
    public void testGetTargetInvokerByAllRules() {
        // initialize the routing rule
        RuleInitializationUtils.initAllRules();
        List<Object> invokers = new ArrayList<>();
        Map<String, String> parameters1 = new HashMap<>();
        parameters1.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", parameters1);
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", parameters1);
        invokers.add(invoker2);
        Map<String, String> parameters2 = new HashMap<>();
        parameters2.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "green");
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1", parameters2);
        invokers.add(invoker3);
        TestObject testObject = new TestObject();
        Invocation invocation = new ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Object[] arguments = new Object[]{invocation};
        Map<String, String> queryMap = testObject.getQueryMap();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "io.sermant.foo.FooTest");
        DubboCache.INSTANCE.setParameters(parameters1);
        DubboCache.INSTANCE.putApplication("io.sermant.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments[0], invokers);
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * test object
     */
    public static class TestObject {
        private final Map<String, String> queryMap;

        public TestObject() {
            queryMap = new HashMap<>();
        }

        public Map<String, String> getQueryMap() {
            return queryMap;
        }
    }

    /**
     * test class
     *
     * @since 2022-09-14
     */
    public static class ApacheInvocation implements Invocation {
        private final Map<String, Object> attachments = new HashMap<>();

        @Override
        public String getTargetServiceUniqueName() {
            return "";
        }

        @Override
        public String getProtocolServiceKey() {
            return "";
        }

        @Override
        public String getMethodName() {
            return "FooTest";
        }

        @Override
        public String getServiceName() {
            return "";
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return new Class[]{String.class, String.class};
        }

        @Override
        public Object[] getArguments() {
            return new Object[]{"foo", "bar"};
        }

        @Override
        public Map<String, String> getAttachments() {
            return MapUtils.objectToStringMap(attachments);
        }

        @Override
        public Map<String, Object> getObjectAttachments() {
            return attachments;
        }

        @Override
        public void setAttachment(String key, String value) {
            attachments.put(key, value);
        }

        @Override
        public void setAttachment(String key, Object value) {
            attachments.put(key, value);
        }

        @Override
        public void setObjectAttachment(String key, Object value) {
            attachments.put(key, value);
        }

        @Override
        public void setAttachmentIfAbsent(String key, String value) {
            attachments.putIfAbsent(key, value);
        }

        @Override
        public void setAttachmentIfAbsent(String key, Object value) {
            attachments.putIfAbsent(key, value);
        }

        @Override
        public void setObjectAttachmentIfAbsent(String key, Object value) {
            attachments.putIfAbsent(key, value);
        }

        @Override
        public String getAttachment(String key) {
            Object value = attachments.get(key);
            return value instanceof String ? (String) value : null;
        }

        @Override
        public String getAttachment(String key, String defaultValue) {
            Object value = attachments.get(key);
            if (value instanceof String) {
                String strValue = (String) value;
                return StringUtils.isBlank(strValue) ? defaultValue : strValue;
            }
            return defaultValue;
        }

        @Override
        public Object getObjectAttachment(String key) {
            return attachments.get(key);
        }

        @Override
        public Object getObjectAttachment(String key, Object defaultValue) {
            return attachments.getOrDefault(key, defaultValue);
        }

        @Override
        public Invoker<?> getInvoker() {
            return null;
        }

        @Override
        public Object put(Object key, Object value) {
            return new Object();
        }

        @Override
        public Object get(Object key) {
            return new Object();
        }

        @Override
        public Map<Object, Object> getAttributes() {
            return Collections.emptyMap();
        }
    }
}
