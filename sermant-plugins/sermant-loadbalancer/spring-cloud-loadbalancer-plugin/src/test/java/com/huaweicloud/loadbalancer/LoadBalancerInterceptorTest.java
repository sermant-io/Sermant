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

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.cache.SpringLoadbalancerCache;
import com.huaweicloud.loadbalancer.interceptor.LoadBalancerInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 测试RandomLoadBalancer/RoundRobinLoadBalancer构造方法的拦截点
 *
 * @author provenceee
 * @see org.springframework.cloud.loadbalancer.core.RandomLoadBalancer
 * @see org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer
 * @since 2022-03-01
 */
public class LoadBalancerInterceptorTest {
    private static final String FOO = "foo";

    private final LoadBalancerInterceptor interceptor;

    /**
     * 构造方法
     */
    public LoadBalancerInterceptorTest() {
        interceptor = new LoadBalancerInterceptor();
    }

    /**
     * 测试不合法的参数
     */
    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        // 测试obj为null
        ExecuteContext context = ExecuteContext.forStaticMethod(Object.class, null, null, null);
        interceptor.after(context);
        Assert.assertEquals(0, getProviderMap().size());
        Assert.assertEquals(0, getOriginCache().size());

        context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);

        // 测试arguments为null
        interceptor.after(context);
        Assert.assertEquals(0, getProviderMap().size());
        Assert.assertEquals(0, getOriginCache().size());

        // 测试参数数组大小小于2
        context = ExecuteContext.forMemberMethod(new Object(), null, new Object[1], null, null);
        interceptor.after(context);
        Assert.assertEquals(0, getProviderMap().size());
        Assert.assertEquals(0, getOriginCache().size());

        // 测试参数数组大小大于1
        Object[] arguments = new Object[2];
        Object obj = new Object();
        context = ExecuteContext.forMemberMethod(obj, null, arguments, null, null);

        // 测试arguments[0]为null
        arguments[0] = null;
        arguments[1] = FOO;
        interceptor.after(context);
        Assert.assertEquals(0, getProviderMap().size());
        Assert.assertEquals(0, getOriginCache().size());

        // 测试arguments[1]为null
        arguments[0] = new Object();
        arguments[1] = null;
        interceptor.after(context);
        Assert.assertEquals(0, getProviderMap().size());
        Assert.assertEquals(0, getOriginCache().size());

        // 测试arguments[1]为foo
        Object args0 = new Object();
        arguments[0] = args0;
        arguments[1] = FOO;
        interceptor.after(context);
        Assert.assertEquals(args0, SpringLoadbalancerCache.INSTANCE.getProvider(FOO));
        Assert.assertEquals(obj, SpringLoadbalancerCache.INSTANCE.getOrigin(FOO));

        // 测试再次调用
        Object newObj = new Object();
        Object newArgs0 = new Object();
        arguments[0] = newArgs0;
        arguments[1] = FOO;
        context = ExecuteContext.forMemberMethod(newObj, null, arguments, null, null);
        interceptor.after(context);
        Assert.assertEquals(args0, SpringLoadbalancerCache.INSTANCE.getProvider(FOO));
        Assert.assertEquals(obj, SpringLoadbalancerCache.INSTANCE.getOrigin(FOO));
    }

    private Map<String, Object> getProviderMap() throws NoSuchFieldException, IllegalAccessException {
        return getCacheMap("providerMap");
    }

    private Map<String, Object> getOriginCache() throws NoSuchFieldException, IllegalAccessException {
        return getCacheMap("originCache");
    }

    private Map<String, Object> getCacheMap(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = SpringLoadbalancerCache.INSTANCE.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Map<String, Object>) field.get(SpringLoadbalancerCache.INSTANCE);
    }
}
