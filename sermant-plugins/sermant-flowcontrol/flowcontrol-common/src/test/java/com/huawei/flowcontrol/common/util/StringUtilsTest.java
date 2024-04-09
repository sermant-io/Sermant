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

package com.huawei.flowcontrol.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * string tool test class
 *
 * @author zhouss
 * @since 2022-03-03
 */
public class StringUtilsTest {
    /**
     * test equality
     */
    @Test
    public void testEqual() {
        String key = "test";
        Assert.assertTrue(StringUtils.equal(key, key));
        Assert.assertTrue(StringUtils.equalIgnoreCase(key, "Test"));
        Assert.assertTrue(StringUtils.equalIgnoreCase(key, "teST"));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.contains(key, "tes"));
        Assert.assertTrue(StringUtils.prefix(key, "te"));
        Assert.assertFalse(StringUtils.prefix(key, "check"));
        Assert.assertTrue(StringUtils.suffix(key, "st"));
        Assert.assertFalse(StringUtils.suffix(key, "sT"));
        Assert.assertFalse(StringUtils.suffix(key, null));
        Assert.assertTrue(StringUtils.equal(StringUtils.trim(" test "), key));
    }
}
