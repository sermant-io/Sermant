/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.gray.dubbo.strategy.type.ArrayTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EmptyTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.EnabledTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ListTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.MapTypeStrategy;
import com.huawei.gray.dubbo.strategy.type.ObjectTypeStrategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 规则策略选择器测试
 *
 * @author pengyuyi
 * @date 2021/12/1
 */
public class TypeStrategyChooserTest {

    private TypeStrategyChooser chooser;

    @Before
    public void before() {
        chooser = TypeStrategyChooser.INSTANCE;
    }

    @Test
    public void testArray() {
        Assert.assertTrue(chooser.choose("[0]") instanceof ArrayTypeStrategy);
    }

    @Test
    public void testEmpty() {
        Assert.assertTrue(chooser.choose("") instanceof EmptyTypeStrategy);
    }

    @Test
    public void testEnabled() {
        Assert.assertTrue(chooser.choose(".isEnabled()") instanceof EnabledTypeStrategy);
    }

    @Test
    public void testList() {
        Assert.assertTrue(chooser.choose(".get(0)") instanceof ListTypeStrategy);
    }

    @Test
    public void testMap() {
        Assert.assertTrue(chooser.choose(".get(\"foo\")") instanceof MapTypeStrategy);
    }

    @Test
    public void testObject() {
        Assert.assertTrue(chooser.choose(".bar") instanceof ObjectTypeStrategy);
    }

}
