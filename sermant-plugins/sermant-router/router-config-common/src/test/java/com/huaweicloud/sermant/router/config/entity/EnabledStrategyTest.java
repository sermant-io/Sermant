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

package com.huaweicloud.sermant.router.config.entity;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * 测试EnabledStrategy
 *
 * @author provenceee
 * @since 2022-10-10
 */
public class EnabledStrategyTest {

    /**
     * 测试reset方法
     */
    @Test
    public void testReset() {
        EnabledStrategy strategy = new EnabledStrategy();
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());

        strategy.reset(Strategy.BLACK, Collections.singletonList("foo"));
        Assert.assertEquals(Strategy.BLACK, strategy.getStrategy());
        Assert.assertEquals(1, strategy.getValue().size());
        Assert.assertEquals("foo", strategy.getValue().get(0));

        strategy.reset();
        Assert.assertEquals(Strategy.NONE, strategy.getStrategy());
        Assert.assertEquals(0, strategy.getValue().size());
    }

    /**
     * 测试全部生效策略
     */
    @Test
    public void testAll() {
        Assert.assertTrue(Strategy.ALL.isMatch(Collections.emptyList(), ""));
    }

    /**
     * 测试全部生效策略
     */
    @Test
    public void testNone() {
        Assert.assertFalse(Strategy.NONE.isMatch(Collections.emptyList(), ""));
    }

    /**
     * 测试白名单策略
     */
    @Test
    public void testWhite() {
        Assert.assertTrue(Strategy.WHITE.isMatch(Collections.singletonList("foo"), "foo"));
        Assert.assertFalse(Strategy.WHITE.isMatch(Collections.singletonList("foo"), "bar"));
        Assert.assertFalse(Strategy.WHITE.isMatch(Collections.emptyList(), ""));
    }

    /**
     * 测试黑名单策略
     */
    @Test
    public void testBlack() {
        Assert.assertFalse(Strategy.BLACK.isMatch(Collections.singletonList("foo"), "foo"));
        Assert.assertTrue(Strategy.BLACK.isMatch(Collections.singletonList("foo"), "bar"));
        Assert.assertTrue(Strategy.BLACK.isMatch(Collections.emptyList(), ""));
    }
}