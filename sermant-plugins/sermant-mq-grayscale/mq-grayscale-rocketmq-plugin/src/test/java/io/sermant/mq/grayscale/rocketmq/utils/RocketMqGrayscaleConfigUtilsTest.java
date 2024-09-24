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

package io.sermant.mq.grayscale.rocketmq.utils;

import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;

import org.apache.rocketmq.common.message.Message;
import org.junit.Assert;
import org.junit.Test;

/**
 * RocketMqGrayscaleConfigUtils test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqGrayscaleConfigUtilsTest extends RocketMqTestAbstract {
    @Test
    public void testGetGrayGroupTag() {
        Assert.assertEquals("gray", RocketMqGrayscaleConfigUtils.getGrayGroupTag());
    }

    @Test
    public void testGetConsumeType() {
        Assert.assertSame(RocketMqGrayscaleConfigUtils.getConsumeType(), ConsumeModeEnum.AUTO);
    }

    @Test
    public void testGetAutoCheckDelayTime() {
        Assert.assertSame(RocketMqGrayscaleConfigUtils.getAutoCheckDelayTime(), 15L);
    }

    @Test
    public void testStandardFormatGroupTag() {
        Assert.assertEquals("gray-group", RocketMqGrayscaleConfigUtils.standardFormatGroupTag("grAY.Group"));
    }

    @Test
    public void testInjectTrafficTagByServiceMeta() {
        Message message = new Message();
        RocketMqGrayscaleConfigUtils.injectTrafficTagByServiceMeta(message);
        Assert.assertEquals("gray", message.getProperty("x_lane_canary"));
    }

    @Test
    public void testGetGrayTagItemByExcludeGroupTags() {
        Assert.assertEquals(1, RocketMqGrayscaleConfigUtils.getGrayTagItemByExcludeGroupTags().size());
    }
}
