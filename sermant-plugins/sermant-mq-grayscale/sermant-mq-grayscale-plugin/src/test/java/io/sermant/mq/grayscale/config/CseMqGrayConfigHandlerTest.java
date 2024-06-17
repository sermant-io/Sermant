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

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * CseMqGrayConfigHandler test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class CseMqGrayConfigHandlerTest extends AbstactMqGrayTest {
    @Test
    public void testHandle() {
        CseMqGrayConfigHandler handler = new CseMqGrayConfigHandler();
        JSONObject object = ConfigContextUtils.buildJSONObject(null);
        ConfigContextUtils.createMqGrayConfig(null);
        Assert.assertTrue(MqGrayscaleConfigUtils.isPlugEnabled());
        String key = "grayscale.mq.config";
        DynamicConfigEvent event = new DynamicConfigEvent(key, "default", object.toString(),
                DynamicConfigEventType.DELETE);
        handler.handle(event);
        Assert.assertFalse(MqGrayscaleConfigUtils.isPlugEnabled());
        key = "gray.mq.config";
        event = new DynamicConfigEvent(key, "default", object.toString(), DynamicConfigEventType.CREATE);
        handler.handle(event);
        Assert.assertFalse(MqGrayscaleConfigUtils.isPlugEnabled());
    }
}
