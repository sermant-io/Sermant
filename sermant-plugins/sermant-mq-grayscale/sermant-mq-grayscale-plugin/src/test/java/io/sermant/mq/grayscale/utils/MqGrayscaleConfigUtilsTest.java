/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.utils;

import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * MqGrayscaleConfigUtils test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGrayscaleConfigUtilsTest extends AbstactMqGrayTest {
    @Test
    public void testGetGrayEnvTag() {
        ConfigContextUtils.createMqGrayConfig(null);
        Assert.assertEquals("test%gray", MqGrayscaleConfigUtils.getGrayEnvTag());
    }

    @Test
    public void testGetTrafficGrayTag() {
        ConfigContextUtils.createMqGrayConfig(null);
        ConfigContextUtils.setTrafficTag();
        Assert.assertEquals("test%gray", MqGrayscaleConfigUtils.getTrafficGrayTag());
        TrafficUtils.removeTrafficTag();
    }

    @Test
    public void testIsExcludeTagsContainsTag() {
        ConfigContextUtils.createMqGrayConfig(null);
        Assert.assertTrue(MqGrayscaleConfigUtils.isExcludeTagsContainsTag("test%gray"));
    }

    @Test
    public void testModifyExcludeTags() {
        ConfigContextUtils.createMqGrayConfig(null);
        Set<String> excludeTags = new HashSet<>();
        excludeTags.add("group%red");
        MqGrayscaleConfigUtils.modifyExcludeTags(excludeTags);
        Assert.assertEquals(2, MqGrayscaleConfigUtils.getExcludeTagsForSet().size());
    }
}
