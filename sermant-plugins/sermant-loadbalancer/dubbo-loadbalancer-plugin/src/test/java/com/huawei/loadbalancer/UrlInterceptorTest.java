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

package com.huawei.loadbalancer;

import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huawei.loadbalancer.interceptor.UrlInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * 测试URL getMethodParameter方法的拦截点
 *
 * @author provenceee
 * @see com.alibaba.dubbo.common.URL
 * @see org.apache.dubbo.common.URL
 * @since 2022-03-01
 */
public class UrlInterceptorTest {
    private final LoadbalancerConfig config;

    private final UrlInterceptor interceptor;

    private final Method method;

    /**
     * 构造方法
     */
    public UrlInterceptorTest() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        interceptor = new UrlInterceptor();
        config = new LoadbalancerConfig();
        Field field = interceptor.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, config);
        method = String.class.getMethod("trim");
    }

    /**
     * 测试不合法的参数
     */
    @Test
    public void testInvalidArguments() {
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), method, null, null, null);

        // 测试arguments为null
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // 测试参数数组大小小于2
        context = ExecuteContext.forMemberMethod(new Object(), method, new Object[1], null, null);
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // 测试参数数组大小大于1
        Object[] arguments = new Object[2];
        context = ExecuteContext.forMemberMethod(new Object(), method, arguments, null, null);

        // 测试arguments[1]为null
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // 测试arguments[1]为bar
        arguments[1] = "bar";
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
    }

    /**
     * 测试合法的参数
     */
    @Test
    public void test() {
        // 测试参数数组大小大于1
        Object[] arguments = new Object[2];
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), method, arguments, null, null);
        arguments[1] = "loadbalance";

        // 测试配置为null
        UrlInterceptor nullConfigInterceptor = new UrlInterceptor();
        nullConfigInterceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        // 测试负载均衡策略为null
        config.setDubboType(null);
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        // 测试正常情况
        config.setDubboType(DubboLoadbalancerType.RANDOM);
        interceptor.before(context);
        Assert.assertTrue(context.isSkip());
        Assert.assertEquals(config.getDubboType().name().toLowerCase(Locale.ROOT), context.getResult());
    }
}
