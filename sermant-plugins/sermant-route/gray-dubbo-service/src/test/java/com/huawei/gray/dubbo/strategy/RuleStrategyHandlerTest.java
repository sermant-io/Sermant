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

package com.huawei.gray.dubbo.strategy;

import com.huawei.route.common.gray.addr.AddrCache;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Tags;
import com.huawei.route.common.gray.label.entity.VersionFrom;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 流量灰度策略测试
 *
 * @author provenceee
 * @since 2022-03-22
 */
public class RuleStrategyHandlerTest {
    private static final URL ALIBABA_URL = URL.valueOf("dubbo://localhost:8080/com.huawei.foo.BarTest?bar=foo");

    private static final org.apache.dubbo.common.URL APACHE_URL = org.apache.dubbo.common.URL
        .valueOf("dubbo://localhost:8080/com.huawei.foo.FooTest?foo=bar");

    private final List<Route> routes;

    /**
     * 构造方法
     */
    public RuleStrategyHandlerTest() {
        routes = new ArrayList<>();
        Tags tags1 = new Tags();
        tags1.setVersion("0.0.1");
        Route route1 = new Route();
        route1.setTags(tags1);
        routes.add(route1);
        Tags tags2 = new Tags();
        tags2.setVersion("0.0.2");
        Route route2 = new Route();
        route2.setTags(tags2);
        routes.add(route2);
        AddrCache.setRegisterVersionCache("localhost:8081", "0.0.1");
        AddrCache.setRegisterVersionCache("localhost:8082", "0.0.2");
    }

    /**
     * 测试alibaba invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testAlibabaV1() {
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>(8081, "0.0.1");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>(8082, "0.0.2");
        invokers.add(invoker2);
        List<Object> targetInvoker = RuleStrategyHandler.WEIGHT
            .getTargetInvoker(routes, invokers, VersionFrom.REGISTER_MSG);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker1, targetInvoker.get(0));
    }

    /**
     * 测试alibaba invoker未命中0.0.1版本实例的情况
     */
    @Test
    public void testAlibabaNotMatch() {
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>(8081, "0.0.1");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>(8082, "0.0.2");
        invokers.add(invoker2);
        routes.get(0).setWeight(0);
        List<Object> targetInvoker = RuleStrategyHandler.WEIGHT
            .getTargetInvoker(routes, invokers, VersionFrom.REGISTER_MSG);
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker2, targetInvoker.get(0));
    }

    /**
     * 测试apache invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testApacheV1() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>(8081, "0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>(8082, "0.0.2");
        invokers.add(invoker2);
        List<Object> targetInvoker = RuleStrategyHandler.WEIGHT
            .getTargetInvoker(routes, invokers, VersionFrom.REGISTER_URL);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker1, targetInvoker.get(0));
    }

    /**
     * 测试apache invoker未命中0.0.1版本实例的情况
     */
    @Test
    public void testApacheNotMatch() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>(8081, "0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>(8082, "0.0.2");
        invokers.add(invoker2);
        routes.get(0).setWeight(0);
        List<Object> targetInvoker = RuleStrategyHandler.WEIGHT
            .getTargetInvoker(routes, invokers, VersionFrom.REGISTER_URL);
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker2, targetInvoker.get(0));
    }

    /**
     * 测试类
     *
     * @since 2022-03-18
     */
    public static class AlibabaInvoker<T> implements Invoker<T> {
        private final URL url;

        /**
         * 构造方法
         *
         * @param port 端口
         * @param version 版本
         */
        public AlibabaInvoker(int port, String version) {
            this.url = ALIBABA_URL.addParameter(GrayConstant.GRAY_VERSION_KEY, version).setPort(port);
        }

        @Override
        public Class<T> getInterface() {
            return null;
        }

        @Override
        public Result invoke(Invocation invocation) throws RpcException {
            return null;
        }

        @Override
        public URL getUrl() {
            return url;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {
        }
    }

    /**
     * 测试类
     *
     * @since 2022-03-18
     */
    public static class ApacheInvoker<T> implements org.apache.dubbo.rpc.Invoker<T> {
        private final org.apache.dubbo.common.URL url;

        /**
         * 构造方法
         *
         * @param port 端口
         * @param version 版本
         */
        public ApacheInvoker(int port, String version) {
            this.url = APACHE_URL.addParameter(GrayConstant.GRAY_VERSION_KEY, version).setPort(port);
        }

        @Override
        public Class<T> getInterface() {
            return null;
        }

        @Override
        public org.apache.dubbo.rpc.Result invoke(org.apache.dubbo.rpc.Invocation invocation)
            throws org.apache.dubbo.rpc.RpcException {
            return null;
        }

        @Override
        public org.apache.dubbo.common.URL getUrl() {
            return url;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {
        }
    }
}