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

package io.sermant.mq.grayscale.config.rocketmq;

import io.sermant.mq.grayscale.config.CommonConfigUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * RocketMqConfigUtils test
 *
 * @author chengyouling
 * @since 2024-09-18
 **/
public class RocketMqConfigUtilsTest {
    @Test
    public void testBaseGroupTagChangeMap() {
        RocketMqConfigUtils.setBaseGroupTagChangeMap("127.0.0.1@TEST@base", true);
        Assert.assertTrue(RocketMqConfigUtils.getBaseGroupTagChangeMap("127.0.0.1@TEST@base"));
        Assert.assertFalse(RocketMqConfigUtils.getBaseGroupTagChangeMap("127.0.0.1@TEST@base1"));
    }

    @Test
    public void testGrayGroupTagChangeMap() {
        RocketMqConfigUtils.setGrayGroupTagChangeMap("127.0.0.1@TEST@base-gray", true);
        Assert.assertTrue(RocketMqConfigUtils.getGrayGroupTagChangeMap("127.0.0.1@TEST@base-gray"));
        Assert.assertFalse(RocketMqConfigUtils.getGrayGroupTagChangeMap("127.0.0.1@TEST@base-gray1"));
    }

    @Test
    public void testUpdateChangeFlag() {
        RocketMqConfigUtils.setBaseGroupTagChangeMap("127.0.0.1@TEST@base", false);
        RocketMqConfigUtils.setGrayGroupTagChangeMap("127.0.0.1@TEST@base-gray", false);
        RocketMqConfigUtils.updateChangeFlag();
        Assert.assertTrue(RocketMqConfigUtils.getBaseGroupTagChangeMap("127.0.0.1@TEST@base"));
        Assert.assertTrue(RocketMqConfigUtils.getGrayGroupTagChangeMap("127.0.0.1@TEST@base-gray"));
    }

    @Test
    public void testRecordTrafficTagsSet() {
        RocketMqConfigUtils.recordTrafficTagsSet(CommonConfigUtils.getMqGrayscaleConfig());
        Assert.assertSame(1, RocketMqConfigUtils.getGrayTagsSet().size());
    }
}
