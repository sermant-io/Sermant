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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.label.LabelCache;
import com.huaweicloud.sermant.router.config.label.entity.Match;
import com.huaweicloud.sermant.router.config.label.entity.MatchRule;
import com.huaweicloud.sermant.router.config.label.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.label.entity.Route;
import com.huaweicloud.sermant.router.config.label.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.label.entity.Rule;
import com.huaweicloud.sermant.router.config.label.entity.ValueMatch;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;
import com.huaweicloud.sermant.router.dubbo.cache.DubboCache;

import org.apache.dubbo.common.utils.MapUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试AbstractDirectoryService
 *
 * @author provenceee
 * @since 2022-09-14
 */
public class AbstractDirectoryServiceTest {
    private final AbstractDirectoryService service;

    public AbstractDirectoryServiceTest() {
        service = new AbstractDirectoryServiceImpl();
    }

    @Before
    public void clear() {
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "");
        LabelCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试无效时
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

        // 测试arguments为null
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, null, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // 测试arguments为空
        targetInvokers = (List<Object>) service.selectInvokers(testObject, new Object[0], invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // 设置arguments
        Object[] arguments = {invocation};

        // 测试路由规则无效
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // 初始化路由规则
        init();

        // 测试queryMap为空
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // side不为consumer
        testObject.getQueryMap().put("side", "");
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());

        // targetService为空
        testObject.getQueryMap().put("side", "consumer");
        testObject.getQueryMap().put("interface", "com.huaweicloud.foo.FooTest");
        targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(invokers, targetInvokers);
        Assert.assertEquals(2, targetInvokers.size());
    }

    @Test
    public void testGetTargetInvoker() {
        // 初始化路由规则
        init();

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
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
    }

    @Test
    public void testGetMissMatchInstances() {
        // 初始化路由规则
        init();

        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        TestObject testObject = new TestObject();
        Invocation invocation = new ApacheInvocation();
        invocation.setAttachment("bar", "bar2");
        Object[] arguments = new Object[]{invocation};
        Map<String, String> queryMap = testObject.getQueryMap();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) service.selectInvokers(testObject, arguments, invokers);
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
    }

    private void init() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList);
        Match match = new Match();
        match.setAttachments(attachments);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        Map<String, List<Rule>> map = new HashMap<>();
        map.put("foo", ruleList);
        RouterConfiguration configuration = LabelCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }

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
     * 测试类
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