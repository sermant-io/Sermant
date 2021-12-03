/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.service.dynamicconfig.kie;

import com.huawei.javamesh.core.service.dynamicconfig.utils.LabelGroupUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试key工具
 *
 * @author zhouss
 * @since 2021-11-23
 */
public class LabelGroupUtilsTest {

    @Test
    public void testKeyUtil() {
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("app", "sc");
        objectObjectHashMap.put("version", "1.0");
        final String labelKey = LabelGroupUtils.createLabelGroup(objectObjectHashMap);
        Assert.assertEquals("KIE-LABEL-FLAG#label=app%3Asc&label=version%3A1.0&", labelKey);
        final Map<String, String> stringStringMap = LabelGroupUtils.resolveGroupLabels(labelKey);
        Assert.assertTrue(stringStringMap.containsKey("app") && stringStringMap.containsKey("version"));
    }
}
