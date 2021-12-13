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

package com.huawei.route.common.gray.label.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 值匹配策略测试
 *
 * @author pengyuyi
 * @date 2021/12/1
 */
public class MatchStrategyTest {
    private MatchStrategy exact;

    private MatchStrategy regex;

    private MatchStrategy noEqu;

    private MatchStrategy noLess;

    private MatchStrategy noGreater;

    private MatchStrategy greater;

    private MatchStrategy less;

    private MatchStrategy in;

    private MatchStrategy prefix;

    private List<String> strValues;
    private List<String> intValues;
    private List<String> regexValues;

    @Before
    public void before() {
        exact = MatchStrategy.EXACT;
        regex = MatchStrategy.REGEX;
        noEqu = MatchStrategy.NOEQU;
        noLess = MatchStrategy.NOLESS;
        noGreater = MatchStrategy.NOGREATER;
        greater = MatchStrategy.GREATER;
        less = MatchStrategy.LESS;
        in = MatchStrategy.IN;
        prefix = MatchStrategy.PREFIX;
        strValues = new ArrayList<String>();
        strValues.add("foo");
        strValues.add("bar");
        intValues = new ArrayList<String>();
        intValues.add("10");
        regexValues = new ArrayList<String>();
        regexValues.add("^bar.*");
    }

    @Test
    public void testExact() {
        Assert.assertTrue(exact.isMatch(strValues, "foo", false));
    }

    @Test
    public void testRegex() {
        Assert.assertTrue(regex.isMatch(regexValues, "bar", false));
    }

    @Test
    public void testNoEqu() {
        Assert.assertTrue(noEqu.isMatch(strValues, "bar", false));
    }

    @Test
    public void testNoLess() {
        Assert.assertTrue(noLess.isMatch(intValues, "10", false));
    }

    @Test
    public void testNoGreater() {
        Assert.assertTrue(noGreater.isMatch(intValues, "10", false));
    }

    @Test
    public void testGreater() {
        Assert.assertTrue(greater.isMatch(intValues, "11", false));
    }

    @Test
    public void testLess() {
        Assert.assertTrue(less.isMatch(intValues, "9", false));
    }

    @Test
    public void testIn() {
        Assert.assertTrue(in.isMatch(strValues, "bar", false));
    }

    @Test
    public void testPrefix() {
        Assert.assertTrue(prefix.isMatch(strValues, "foobar", false));
    }
}
