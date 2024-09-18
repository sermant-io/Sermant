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

package io.sermant.mq.grayscale.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GrayTagItem test
 *
 * @author chengyouling
 * @since 2024-09-18
 **/
public class GrayTagItemTest {
    @Test
    public void testMatchPropertiesByServiceMeta() {
        Map<String, String> properties = new HashMap<>();
        properties.put("x_lane_tag", "gray");
        GrayTagItem item = CommonConfigUtils.getGrayTagItem();
        Assert.assertTrue(item.matchPropertiesByServiceMeta(properties));

        properties.put("x_lane_tag", "red");
        Assert.assertFalse(item.matchPropertiesByServiceMeta(properties));
    }

    @Test
    public void testUpdateTrafficTags() {
        GrayTagItem itemResource = CommonConfigUtils.getGrayTagItem();
        List<GrayTagItem> grayscale = new ArrayList<>();
        GrayTagItem item = CommonConfigUtils.getGrayTagItem();
        item.getTrafficTag().put("x_lane_red", "red");
        grayscale.add(item);
        itemResource.updateTrafficTags(grayscale);
        Assert.assertSame(2, itemResource.getTrafficTag().size());

        itemResource = CommonConfigUtils.getGrayTagItem();
        itemResource.setConsumerGroupTag("red");
        itemResource.updateTrafficTags(grayscale);
        Assert.assertSame(1, itemResource.getTrafficTag().size());
    }
}
