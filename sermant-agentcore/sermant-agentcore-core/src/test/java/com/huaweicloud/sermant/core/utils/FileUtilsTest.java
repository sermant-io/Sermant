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
 * 测试文件工具类
 *
 * @author lilai
 * @since 2022-10-11
 */
public class FileUtilsTest {
    @Test
    public void testPath() {
        String pathValid = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Assert.assertEquals(pathValid, FileUtils.validatePath(pathValid));
        String pathInvalid = "/test/path";
        Assert.assertEquals("", FileUtils.validatePath(pathInvalid));
        String pathInvalidSymbolA = pathValid + "../" + "/test";
        Assert.assertEquals(pathValid + "/test", FileUtils.validatePath(pathInvalidSymbolA));
        String pathInvalidSymbolB = pathValid + "..\\" + "/test";
        Assert.assertEquals(pathValid + "/test", FileUtils.validatePath(pathInvalidSymbolB));
    }
}