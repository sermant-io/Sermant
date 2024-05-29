/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.util;


import io.sermant.implement.utils.LabelGroupUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * LabelGroup tests
 *
 * @author xzc
 * @since 2024-05-16
 */
public class LabelGroupUtilsTest {
    @Test
    public void testCreateLabelGroup() {
        String result = LabelGroupUtils.createLabelGroup(new HashMap<String, String>() {{
            put("String", "String");
        }});
        Assert.assertEquals("String=String", result);
        String result1 = LabelGroupUtils.createLabelGroup(new HashMap<String, String>() {{
            put("String", "String");
            put("String1", "String1");
        }});
        Assert.assertEquals("String=String&String1=String1", result1);
    }

    @Test
    public void testIsLabelGroup() {
        boolean result = LabelGroupUtils.isLabelGroup("String=String");
        Assert.assertTrue(result);
        boolean result1 = LabelGroupUtils.isLabelGroup("String");
        Assert.assertFalse(result1);
    }

    @Test
    public void testResolveGroupLabels() {
        Map<String, String> result = LabelGroupUtils.resolveGroupLabels("String=String");
        Assert.assertEquals(new HashMap<String, String>() {{
            put("String", "String");
        }}, result);
        Map<String, String> result1 = LabelGroupUtils.resolveGroupLabels("String=String&String1=String1");
        Assert.assertEquals(new HashMap<String, String>() {{
            put("String", "String");
            put("String1", "String1");
        }}, result1);
    }

    @Test
    public void testGetLabelCondition() {
        String result = LabelGroupUtils.getLabelCondition("String=String");
        try {
            Assert.assertEquals("label="+ URLEncoder.encode("String:String", "UTF-8"), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}