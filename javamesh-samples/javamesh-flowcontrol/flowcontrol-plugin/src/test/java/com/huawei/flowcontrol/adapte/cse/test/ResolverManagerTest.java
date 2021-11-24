/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.test;

import com.huawei.flowcontrol.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.adapte.cse.resolver.RateLimitingRuleResolver;
import org.junit.Assert;
import org.junit.Test;

/**
 * 解析器测试
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class ResolverManagerTest {
    @Test
    public void loadResolverTest() {
        Assert.assertTrue(ResolverManager.INSTANCE.getResolversMap().size() > 0);
    }

    @Test
    public void getResolverTest() {
        Assert.assertNotNull(
                ResolverManager.INSTANCE.getResolver(RateLimitingRuleResolver.CONFIG_KEY));
    }
}
