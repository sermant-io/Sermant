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

package io.sermant.router.dubbo.strategy.type;

import io.sermant.router.dubbo.strategy.TypeStrategy;

import org.junit.Assert;
import org.junit.Test;

/**
 * entity matching strategy testing
 *
 * @author provenceee
 * @since 2021-12-01
 */
public class ObjectTypeStrategyTest {
    /**
     * test entity strategy
     */
    @Test
    public void testValue() {
        TypeStrategy strategy = new ObjectTypeStrategy();
        Entity entity = new Entity();
        entity.setTest("bar");

        // Normal
        Assert.assertEquals("bar", strategy.getValue(entity, ".test").orElse(null));

        // Test null
        Assert.assertNotEquals("bar", strategy.getValue(new Entity(), ".test").orElse(null));

        // The test couldn't find methods
        Assert.assertNull(strategy.getValue(new Entity(), ".foo").orElse(null));

        Assert.assertNull(strategy.getValue(new Entity(), ".Foo").orElse(null));

        Assert.assertNull(strategy.getValue(new Entity(), ".$oo").orElse(null));

        // The test is not equal
        entity.setTest("foo");
        Assert.assertNotEquals("bar", strategy.getValue(entity, ".test").orElse(null));
    }
}