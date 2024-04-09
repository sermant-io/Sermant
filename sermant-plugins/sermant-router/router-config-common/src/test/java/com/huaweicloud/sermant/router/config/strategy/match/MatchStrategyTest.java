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

package com.huaweicloud.sermant.router.config.strategy.match;

import com.huaweicloud.sermant.router.config.entity.MatchStrategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value matching strategy test
 *
 * @author provenceee
 * @since 2021-12-01
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

    private List<String> invalidRegexValues;

    private List<String> emptyList;

    private List<String> nullValueList;

    /**
     * Initialize
     */
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
        strValues = new ArrayList<>();
        strValues.add("foo");
        strValues.add("bar");
        intValues = new ArrayList<>();
        intValues.add("10");
        regexValues = new ArrayList<>();
        regexValues.add("^bar.*");
        invalidRegexValues = new ArrayList<>();
        invalidRegexValues.add("*");
        emptyList = Collections.emptyList();
        nullValueList = Collections.singletonList(null);
    }

    /**
     * Equal-value matching strategy
     */
    @Test
    public void testExact() {
        // Normally, test not case-sensitive
        Assert.assertTrue(exact.isMatch(new ArrayList<>(strValues), "FoO", false));

        // Normally, test case-sensitive
        Assert.assertTrue(exact.isMatch(new ArrayList<>(strValues), "foo", true));

        // Test case inconsistencies
        Assert.assertFalse(exact.isMatch(new ArrayList<>(strValues), "foO", true));

        // Test null arg
        Assert.assertFalse(exact.isMatch(new ArrayList<>(strValues), null, false));

        // Test null arg
        Assert.assertFalse(exact.isMatch(new ArrayList<>(strValues), null, true));

        // Test null values
        Assert.assertFalse(exact.isMatch(null, "FoO", false));

        // Test null values
        Assert.assertFalse(exact.isMatch(null, "foo", true));

        // Test empty values
        Assert.assertFalse(exact.isMatch(emptyList, "FoO", false));

        // Test empty values
        Assert.assertFalse(exact.isMatch(emptyList, "foo", true));

        // Test values.get(0) == null
        Assert.assertFalse(exact.isMatch(nullValueList, "FoO", false));

        // Test values.get(0) == null
        Assert.assertFalse(exact.isMatch(nullValueList, "foo", true));
    }

    /**
     * Regular expression matching strategy
     */
    @Test
    public void testRegex() {
        // Normally, it is not case sensitive
        Assert.assertTrue(regex.isMatch(new ArrayList<>(regexValues), "BaR", false));

        // Normally, it is case-sensitive
        Assert.assertTrue(regex.isMatch(new ArrayList<>(regexValues), "bar", true));

        // Test case inconsistencies
        Assert.assertFalse(regex.isMatch(new ArrayList<>(regexValues), "Bar", true));

        // Test null arg
        Assert.assertFalse(regex.isMatch(new ArrayList<>(regexValues), null, false));

        // Test null arg
        Assert.assertFalse(regex.isMatch(new ArrayList<>(regexValues), null, true));

        // Invalid regex for testing
        Assert.assertFalse(regex.isMatch(new ArrayList<>(invalidRegexValues), "a", false));

        // Test null values
        Assert.assertFalse(regex.isMatch(null, "BaR", false));

        // Test null values
        Assert.assertFalse(regex.isMatch(null, "bar", true));

        // Test empty values
        Assert.assertFalse(regex.isMatch(emptyList, "BaR", false));

        // Test empty values
        Assert.assertFalse(regex.isMatch(emptyList, "bar", true));

        // Test values.get(0) == null
        Assert.assertFalse(regex.isMatch(nullValueList, "BaR", false));

        // Test values.get(0) == null
        Assert.assertFalse(regex.isMatch(nullValueList, "bar", true));
    }

    /**
     * Not equal to a matching strategy
     */
    @Test
    public void testNoEqu() {
        // Normally, it is not case sensitive
        Assert.assertFalse(noEqu.isMatch(new ArrayList<>(strValues), "FoO", false));

        // Normally, it is not case sensitive
        Assert.assertTrue(noEqu.isMatch(new ArrayList<>(strValues), "BaR", false));

        // Normally, it is case-sensitive
        Assert.assertFalse(noEqu.isMatch(new ArrayList<>(strValues), "foo", true));

        // Test case inconsistencies
        Assert.assertTrue(noEqu.isMatch(new ArrayList<>(strValues), "Foo", true));

        // Test null arg
        Assert.assertFalse(noEqu.isMatch(new ArrayList<>(strValues), null, false));

        // Test null arg
        Assert.assertFalse(noEqu.isMatch(new ArrayList<>(strValues), null, true));

        // Test null values
        Assert.assertFalse(noEqu.isMatch(null, "FoO", false));

        // Test null values
        Assert.assertFalse(noEqu.isMatch(null, "foo", true));

        // Test empty values
        Assert.assertFalse(noEqu.isMatch(emptyList, "FoO", false));

        // Test empty values
        Assert.assertFalse(noEqu.isMatch(emptyList, "foo", true));

        // Test values.get(0) == null
        Assert.assertFalse(noEqu.isMatch(nullValueList, "FoO", false));

        // Test values.get(0) == null
        Assert.assertFalse(noEqu.isMatch(nullValueList, "foo", true));
    }

    /**
     * Not less than the matching policy
     */
    @Test
    public void testNoLess() {
        // Normally, equals, is not case-sensitive
        Assert.assertTrue(noLess.isMatch(new ArrayList<>(intValues), "10", false));

        // Normally, equals, case-sensitive
        Assert.assertTrue(noLess.isMatch(new ArrayList<>(intValues), "10", true));

        // Normally, greater than, is not case sensitive
        Assert.assertTrue(noLess.isMatch(new ArrayList<>(intValues), "11", false));

        // Normally, greater than, case-sensitive
        Assert.assertTrue(noLess.isMatch(new ArrayList<>(intValues), "11", true));

        // The test is smaller and case-insensitive
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), "9", false));

        // The test is smaller than and case-sensitive
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), "9", true));

        // Test non-numeric
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), "foo", false));

        // Test non-numeric
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), "foo", true));

        // Test null arg
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), null, false));

        // Test null arg
        Assert.assertFalse(noLess.isMatch(new ArrayList<>(intValues), null, true));

        // Test null values
        Assert.assertFalse(noLess.isMatch(null, "10", false));

        // Test null values
        Assert.assertFalse(noLess.isMatch(null, "10", true));

        // Test empty values
        Assert.assertFalse(noLess.isMatch(emptyList, "10", false));

        // Test empty values
        Assert.assertFalse(noLess.isMatch(emptyList, "10", true));

        // Test values.get(0) == null
        Assert.assertFalse(noLess.isMatch(nullValueList, "10", false));

        // Test values.get(0) == null
        Assert.assertFalse(noLess.isMatch(nullValueList, "10", true));
    }

    /**
     * Not greater than the matching policy
     */
    @Test
    public void testNoGreater() {
        // Normally, equals, is not case-sensitive
        Assert.assertTrue(noGreater.isMatch(new ArrayList<>(intValues), "10", false));

        // Normally, equals, case-sensitive
        Assert.assertTrue(noGreater.isMatch(new ArrayList<>(intValues), "10", true));

        // Normally, less than, not case-sensitive
        Assert.assertTrue(noGreater.isMatch(new ArrayList<>(intValues), "9", false));

        // Normally, less than, case-sensitive
        Assert.assertTrue(noGreater.isMatch(new ArrayList<>(intValues), "9", true));

        // The test is larger than and is not case-sensitive
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), "11", false));

        // The test is larger than and case-sensitive
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), "11", true));

        // Test non-numeric
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), "foo", false));

        // Test non-numeric
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), "foo", true));

        // Test null arg
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), null, false));

        // Test null arg
        Assert.assertFalse(noGreater.isMatch(new ArrayList<>(intValues), null, true));

        // Test null values
        Assert.assertFalse(noGreater.isMatch(null, "10", false));

        // Test null values
        Assert.assertFalse(noGreater.isMatch(null, "10", true));

        // Test empty values
        Assert.assertFalse(noGreater.isMatch(emptyList, "10", false));

        // Test empty values
        Assert.assertFalse(noGreater.isMatch(emptyList, "10", true));

        // Test values.get(0) == null
        Assert.assertFalse(noGreater.isMatch(nullValueList, "10", false));

        // Test values.get(0) == null
        Assert.assertFalse(noGreater.isMatch(nullValueList, "10", true));
    }

    /**
     * Greater than the matching strategy
     */
    @Test
    public void testGreater() {
        // Normally, greater than, is not case sensitive
        Assert.assertTrue(greater.isMatch(new ArrayList<>(intValues), "11", false));

        // Normally, greater than, case-sensitive
        Assert.assertTrue(greater.isMatch(new ArrayList<>(intValues), "11", true));

        // The test is equal and case-insensitive
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "10", false));

        // The test is equal to, less than, and case-sensitive
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "10", true));

        // The test is smaller and case-insensitive
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "9", false));

        // The test is smaller than and case-sensitive
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "9", true));

        // Test non-numeric
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "foo", false));

        // Test non-numeric
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), "foo", true));

        // Test null arg
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), null, false));

        // Test null arg
        Assert.assertFalse(greater.isMatch(new ArrayList<>(intValues), null, true));

        // Test null values
        Assert.assertFalse(greater.isMatch(null, "11", false));

        // Test null values
        Assert.assertFalse(greater.isMatch(null, "11", true));

        // Test empty values
        Assert.assertFalse(greater.isMatch(emptyList, "11", false));

        // Test empty values
        Assert.assertFalse(greater.isMatch(emptyList, "11", true));

        // Test values.get(0) == null
        Assert.assertFalse(greater.isMatch(nullValueList, "11", false));

        // Test values.get(0) == null
        Assert.assertFalse(greater.isMatch(nullValueList, "11", true));
    }

    /**
     * Less than the matching policy
     */
    @Test
    public void testLess() {
        // Normally, less than, not case-sensitive
        Assert.assertTrue(less.isMatch(new ArrayList<>(intValues), "9", false));

        // Normally, less than, case-sensitive
        Assert.assertTrue(less.isMatch(new ArrayList<>(intValues), "9", true));

        // The test is equal and case-insensitive
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "10", false));

        // The test is equal to, less than, and case-sensitive
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "10", true));

        // The test is larger than and is not case-sensitive
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "11", false));

        // The test is larger than and case-sensitive
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "11", true));

        // Test non-numeric
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "foo", false));

        // Test non-numeric
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), "foo", true));

        // Test null arg
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), null, false));

        // Test null arg
        Assert.assertFalse(less.isMatch(new ArrayList<>(intValues), null, true));

        // Test null values
        Assert.assertFalse(less.isMatch(null, "9", false));

        // Test null values
        Assert.assertFalse(less.isMatch(null, "9", true));

        // Test empty values
        Assert.assertFalse(less.isMatch(emptyList, "9", false));

        // Test empty values
        Assert.assertFalse(less.isMatch(emptyList, "9", true));

        // Test values.get(0) == null
        Assert.assertFalse(less.isMatch(nullValueList, "9", false));

        // Test values.get(0) == null
        Assert.assertFalse(less.isMatch(nullValueList, "9", true));
    }

    /**
     * Contains a matching policy
     */
    @Test
    public void testIn() {
        // Normally, it is not case sensitive
        Assert.assertTrue(in.isMatch(new ArrayList<>(strValues), "BaR", false));

        // Normally, it is case-sensitive
        Assert.assertTrue(in.isMatch(new ArrayList<>(strValues), "foo", true));

        // Test case inconsistencies
        Assert.assertFalse(in.isMatch(new ArrayList<>(strValues), "foO", true));

        // Test null arg
        Assert.assertFalse(in.isMatch(new ArrayList<>(strValues), null, false));

        // Test null arg
        Assert.assertFalse(in.isMatch(new ArrayList<>(strValues), null, true));

        // Test null values
        Assert.assertFalse(in.isMatch(null, "BaR", false));

        // Test null values
        Assert.assertFalse(in.isMatch(null, "foo", true));

        // Test empty values
        Assert.assertFalse(in.isMatch(emptyList, "BaR", false));

        // Test empty values
        Assert.assertFalse(in.isMatch(emptyList, "foo", true));

        // Test values.get(0) == null
        Assert.assertFalse(in.isMatch(nullValueList, "BaR", false));

        // Test values.get(0) == null
        Assert.assertFalse(in.isMatch(nullValueList, "foo", true));
    }

    /**
     * Prefix matching policy
     */
    @Test
    public void testPrefix() {
        // Normally, it is not case sensitive
        Assert.assertTrue(prefix.isMatch(new ArrayList<>(strValues), "FoOBar", false));

        // Normally, it is case-sensitive
        Assert.assertTrue(prefix.isMatch(new ArrayList<>(strValues), "fooBar", true));

        // Test case inconsistencies
        Assert.assertFalse(prefix.isMatch(new ArrayList<>(strValues), "foOBar", true));

        // Test null arg
        Assert.assertFalse(prefix.isMatch(new ArrayList<>(strValues), null, false));

        // Test null arg
        Assert.assertFalse(prefix.isMatch(new ArrayList<>(strValues), null, true));

        // Test null values
        Assert.assertFalse(prefix.isMatch(null, "FoOBar", false));

        // Test null values
        Assert.assertFalse(prefix.isMatch(null, "fooBar", true));

        // Test empty values
        Assert.assertFalse(prefix.isMatch(emptyList, "FoOBar", false));

        // Test empty values
        Assert.assertFalse(prefix.isMatch(emptyList, "fooBar", true));

        // Test values.get(0) == null
        Assert.assertFalse(prefix.isMatch(nullValueList, "FoOBar", false));

        // Test values.get(0) == null
        Assert.assertFalse(prefix.isMatch(nullValueList, "fooBar", true));
    }
}