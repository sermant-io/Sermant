/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.adapte.cse.match;

import com.huawei.flowcontrol.common.adapte.cse.match.operator.CompareOperator;
import com.huawei.flowcontrol.common.adapte.cse.match.operator.ContainsOperator;
import com.huawei.flowcontrol.common.adapte.cse.match.operator.ExactOperator;
import com.huawei.flowcontrol.common.adapte.cse.match.operator.PrefixOperator;
import com.huawei.flowcontrol.common.adapte.cse.match.operator.SuffixOperator;

import org.junit.Assert;
import org.junit.Test;

/**
 * 比较器测试
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class OperatorTest {
    private final String target = "test";

    /**
     * 测试数字比较
     */
    @Test
    public void testCompare() {
        final CompareOperator compareOperator = new CompareOperator();
        Assert.assertTrue(compareOperator.match("120", "=120"));
        Assert.assertTrue(compareOperator.match("120", ">=120"));
        Assert.assertTrue(compareOperator.match("200", ">120"));
        Assert.assertFalse(compareOperator.match("180.0", "<120"));
        Assert.assertFalse(compareOperator.match("110.0", "!110"));
        Assert.assertTrue(compareOperator.match("160.0", "!120"));
    }

    /**
     * 测试字符串包含
     */
    @Test
    public void testContains() {
        final ContainsOperator containsOperator = new ContainsOperator();
        Assert.assertTrue(containsOperator.match(target, "es"));
        Assert.assertTrue(containsOperator.match(target, target));
    }

    /**
     * 测试相等
     */
    @Test
    public void testExact() {
        final ExactOperator exactOperator = new ExactOperator();
        Assert.assertTrue(exactOperator.match("12.0.0", "12.0.0"));
        Assert.assertFalse(exactOperator.match("11.0.0", "12.0"));
        Assert.assertFalse(exactOperator.match("", ".0"));
    }

    /**
     * 测试前缀
     */
    @Test
    public void testPrefix() {
        final PrefixOperator prefixOperator = new PrefixOperator();
        Assert.assertTrue(prefixOperator.match(target, "te"));
        Assert.assertTrue(prefixOperator.match(target, target));
        Assert.assertFalse(prefixOperator.match(target, "prefix"));
    }

    /**
     * 测试后缀
     */
    @Test
    public void testSuffix() {
        final SuffixOperator suffixOperator = new SuffixOperator();
        Assert.assertTrue(suffixOperator.match(target, "st"));
        Assert.assertTrue(suffixOperator.match(target, target));
        Assert.assertTrue(suffixOperator.match(target, ""));
        Assert.assertFalse(suffixOperator.match(target, "suffix"));
    }
}
