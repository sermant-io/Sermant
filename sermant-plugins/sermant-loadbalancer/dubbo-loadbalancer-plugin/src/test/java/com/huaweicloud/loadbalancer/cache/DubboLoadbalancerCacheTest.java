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

package com.huaweicloud.loadbalancer.cache;

import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * dubbo缓存测试
 *
 * @author zhouss
 * @since 2022-08-16
 */
public class DubboLoadbalancerCacheTest {
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
     * dubbo缓存测试
     */
    @Test
    public void test() {
        final DubboLoadbalancerCache instance = DubboLoadbalancerCache.INSTANCE;

        // 测试发布配置监听
        String serviceName = "test";
        RuleManagerHelper.publishRule(serviceName, DubboLoadbalancerType.RANDOM.getMapperName());
        Assert.assertNull(instance.getNewCache().get(serviceName));

        // 测试发布空服务名, 即匹配全局, 该情况清理负载均衡
        RuleManagerHelper.publishRule(null, DubboLoadbalancerType.SHORTESTRESPONSE.getMapperName());
        Assert.assertNull(instance.getNewCache().get(serviceName));
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceName);
        Assert.assertTrue(targetServiceRule.isPresent());
        final Optional<DubboLoadbalancerType> type = DubboLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
        Assert.assertTrue(type.isPresent());
        Assert.assertEquals(type.get(), DubboLoadbalancerType.SHORTESTRESPONSE);
    }
}
