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

package com.huaweicloud.sermant.router.dubbo.strategy;

import com.huaweicloud.sermant.router.dubbo.strategy.type.Entity;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * rule policy selector testing
 *
 * @author provenceee
 * @since 2021-12-01
 */
public class TypeStrategyChooserTest {
    private final TypeStrategyChooser chooser;

    private final Object[] arguments;

    /**
     * constructor
     */
    public TypeStrategyChooserTest() {
        chooser = TypeStrategyChooser.INSTANCE;
        arguments = new Object[1];
    }

    /**
     * test the array strategy
     */
    @Test
    public void testArray() {
        arguments[0] = new String[]{"foo"};

        // normal
        Assert.assertEquals("foo", chooser.getValue("[0]", "args0", arguments).orElse(null));
    }

    /**
     * test the null strategy
     */
    @Test
    public void testEmpty() {
        arguments[0] = "foo";

        // normal
        Assert.assertEquals("foo", chooser.getValue("", "args0", arguments).orElse(null));
    }

    /**
     * test the enabled policy
     */
    @Test
    public void testEnabled() {
        Entity entity = new Entity();
        entity.setEnabled(true);
        arguments[0] = entity;

        // normal
        Assert.assertEquals(Boolean.TRUE.toString(), chooser.getValue(".isEnabled()", "args0", arguments).orElse(null));
    }

    /**
     * test the list strategy
     */
    @Test
    public void testList() {
        arguments[0] = Collections.singletonList("foo");

        // normal
        Assert.assertEquals("foo", chooser.getValue(".get(0)", "args0", arguments).orElse(null));
    }

    /**
     * test the map policy
     */
    @Test
    public void testMap() {
        arguments[0] = Collections.singletonMap("foo", "bar");

        // normal
        Assert.assertEquals("bar", chooser.getValue(".get(\"foo\")", "args0", arguments).orElse(null));
    }

    /**
     * test entity strategy
     */
    @Test
    public void testObject() {
        Entity entity = new Entity();
        entity.setTest("foo");
        arguments[0] = entity;

        // normal
        Assert.assertEquals("foo", chooser.getValue(".test", "args0", arguments).orElse(null));
    }

    /**
     * Test null (anomaly)
     */
    @Test
    public void testNull() {
        // the parameter is null
        Assert.assertNull(chooser.getValue(".test", "args0", null).orElse(null));

        // not hit TypeStrategy
        Assert.assertNull(chooser.getValue("bar", "args0", arguments).orElse(null));

        // non numeric
        Assert.assertNull(chooser.getValue(".test", "argsA", arguments).orElse(null));

        // the index is out of bounds
        Assert.assertNull(chooser.getValue(".test", "args1", arguments).orElse(null));
    }

    /**
     * test non numeric
     */
    @Test
    public void testInvalidNumber() {
        arguments[0] = new String[]{"foo"};

        // test non numeric
        Assert.assertNull(chooser.getValue("[bar]", "args0", arguments).orElse(null));
    }
}