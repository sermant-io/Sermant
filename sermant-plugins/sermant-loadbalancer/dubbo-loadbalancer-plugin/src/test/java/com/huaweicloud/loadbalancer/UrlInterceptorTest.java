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

import com.huaweicloud.loadbalancer.cache.DubboLoadbalancerCache;
import com.huaweicloud.loadbalancer.cache.RuleManagerHelper;
import com.huaweicloud.loadbalancer.cache.YamlRuleConverter;
import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.interceptor.UrlInterceptor;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.apache.dubbo.common.URL;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 测试URL getMethodParameter方法的拦截点
 *
 * @author provenceee
 * @see com.alibaba.dubbo.common.URL
 * @see org.apache.dubbo.common.URL
 * @since 2022-03-01
 */
public class UrlInterceptorTest {
    private static final String SERVICE_NAME = "test";

    /**
     * 配置转换器
     */
    @BeforeClass
    public static void setUp() {
        Mockito.mockStatic(ServiceManager.class)
                .when(() -> ServiceManager.getService(RuleConverter.class))
                .thenReturn(new YamlRuleConverter());
    }

    /**
     * 测试不合法的参数
     */
    @Test
    public void testInvalidArguments() throws NoSuchMethodException {
        ExecuteContext context = buildContext(null);
        final UrlInterceptor interceptor = new UrlInterceptor();
        // 测试arguments为null
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // 测试参数数组大小小于2
        context = buildContext(new Object[1]);
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());

        // 测试参数数组大小大于1
        Object[] arguments = new Object[2];
        context = buildContext(arguments);

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
    public void test() throws NoSuchMethodException {
        // 测试参数数组大小大于1
        Object[] arguments = new Object[2];
        ExecuteContext context = buildContext(arguments);
        arguments[1] = "loadbalance";

        // 测试配置为null
        UrlInterceptor nullConfigInterceptor = new UrlInterceptor();
        nullConfigInterceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        Mockito.mockStatic(PluginConfigManager.class)
                .when(() -> PluginConfigManager.getPluginConfig(LoadbalancerConfig.class))
                .thenReturn(new LoadbalancerConfig());
        final UrlInterceptor interceptor = new UrlInterceptor();
        // 测试负载均衡策略为null
        interceptor.before(context);
        Assert.assertFalse(context.isSkip());
        Assert.assertNull(context.getResult());

        // 测试正常情况
        final DubboLoadbalancerCache instance = DubboLoadbalancerCache.INSTANCE;
        RuleManagerHelper.publishRule(SERVICE_NAME, DubboLoadbalancerType.SHORTESTRESPONSE.getMapperName());
        interceptor.before(context);
        Assert.assertNotNull(instance.getNewCache().get(SERVICE_NAME));
        Assert.assertTrue(context.isSkip());
    }

    private ExecuteContext buildContext(Object[] arguments) throws NoSuchMethodException {
        final URL url = new URL("dubbo", "localhost", 8080, null, "remote.application", SERVICE_NAME);
        return ExecuteContext.forMemberMethod(url, String.class.getDeclaredMethod("trim"), arguments,
                Collections.emptyMap(),
                Collections.emptyMap());
    }
}
