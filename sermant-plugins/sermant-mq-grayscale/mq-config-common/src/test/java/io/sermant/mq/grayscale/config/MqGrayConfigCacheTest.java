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

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * MqGrayConfigCache test
 *
 * @author chengyouling
 * @since 2024-09-18
 **/
public class MqGrayConfigCacheTest {
    @Test
    public void testSetCacheConfig() {
        MqGrayscaleConfig config = getMqGrayscaleConfig(CommonConfigUtils.getGrayTagItem());
        MqGrayConfigCache.setCacheConfig(config, DynamicConfigEventType.CREATE);
        Assert.assertTrue(MqGrayConfigCache.getCacheConfig().isEnabled());

        // consumeMode changed will not update config
        config.getBase().setConsumeMode(ConsumeModeEnum.BASE);
        MqGrayConfigCache.setCacheConfig(config, DynamicConfigEventType.MODIFY);
        Assert.assertSame(ConsumeModeEnum.BASE, MqGrayConfigCache.getCacheConfig().getBase().getConsumeMode());

        // serviceMeta changed will not update config
        GrayTagItem item = CommonConfigUtils.getGrayTagItem();
        item.getServiceMeta().put("x_lane_tag", "red" );
        MqGrayscaleConfig config1 = getMqGrayscaleConfig(item);
        MqGrayConfigCache.setCacheConfig(config1, DynamicConfigEventType.MODIFY);
        Assert.assertEquals("gray",
                MqGrayConfigCache.getCacheConfig().getGrayscale().get(0).getServiceMeta().get("x_lane_tag"));

        // trafficTag changed will not update config
        GrayTagItem item1 = CommonConfigUtils.getGrayTagItem();
        item1.getTrafficTag().put("x_lane_canary", "red");
        item1.getServiceMeta().put("x_lane_tag", "red" );
        MqGrayscaleConfig config2 = getMqGrayscaleConfig(item1);
        MqGrayConfigCache.setCacheConfig(config2, DynamicConfigEventType.MODIFY);
        Assert.assertEquals("red",
                MqGrayConfigCache.getCacheConfig().getGrayscale().get(0).getTrafficTag().get("x_lane_canary"));
    }

    private MqGrayscaleConfig getMqGrayscaleConfig(GrayTagItem item) {
        MqGrayscaleConfig config = new MqGrayscaleConfig();
        config.setEnabled(true);
        config.setGrayscale(CommonConfigUtils.getGrayscale(item));
        return config;
    }
}
