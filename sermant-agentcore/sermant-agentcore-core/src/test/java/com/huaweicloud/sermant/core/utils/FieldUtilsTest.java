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

package com.huaweicloud.sermant.core.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 字段格式转换测试类
 *
 * @author xzc
 * @since 2022-10-08
 */
public class FieldUtilsTest {

    @Test
    public void testToUpperUnderline() {
        String result = FieldUtils.toUpperUnderline("ToUpperUnderline");
        Assert.assertEquals("TO_UPPER_UNDERLINE", result);
    }

    @Test
    public void testToLowerUnderline() {
        String result = FieldUtils.toLowerUnderline("ToLowerUnderline");
        Assert.assertEquals("to_lower_underline", result);
    }

    @Test
    public void testToUnderline() {
        String result = FieldUtils.toUnderline("ToUnderline", true);
        Assert.assertEquals("TO_UNDERLINE", result);
        String result1 = FieldUtils.toUnderline("ToUnderline", false);
        Assert.assertEquals("to_underline", result1);
    }

    @Test
    public void testToUnderline2() {
        String result = FieldUtils.toUnderline("ToUnderline", '-', true);
        Assert.assertEquals("TO-UNDERLINE", result);
        String result1 = FieldUtils.toUnderline("ToUnderline", '-', false);
        Assert.assertEquals("to-underline", result1);
    }

    @Test
    public void testToUpperCamel() {
        String result = FieldUtils.toUpperCamel("ToUpperCamel");
        Assert.assertEquals("TO_UPPER_CAMEL", result);
    }

    @Test
    public void testToLowerCamel() {
        String result = FieldUtils.toLowerCamel("ToLowerCamel");
        Assert.assertEquals("to_lower_camel", result);
    }

    @Test
    public void testToCamel() {
        String result = FieldUtils.toCamel("ToCamel", true);
        Assert.assertEquals("TO_CAMEL", result);
        String result1 = FieldUtils.toCamel("ToCamel", false);
        Assert.assertEquals("to_camel", result1);
    }

    @Test
    public void testToCamel2() {
        String result = FieldUtils.toCamel("To-Camel", '-', true);
        Assert.assertEquals("ToCamel", result);
        String result1 = FieldUtils.toCamel("To-Camel", '-', false);
        Assert.assertEquals("toCamel", result1);
    }
}