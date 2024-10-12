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

package io.sermant.backend.controller;

import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.hotplugging.CommandType;
import io.sermant.backend.entity.hotplugging.HotPluggingConfig;
import io.sermant.backend.service.HotPluggingService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test class for HotPluggingController class
 *
 * @author zhp
 * @since 2024-08-27
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HotPluggingService.class})
public class HotPluggingControllerTest {
    private static final String INSTANCE_ID = "82ea03ab-b553-4b24-a4cc-a8c06611ea68";

    @Mock
    private HotPluggingService hotPluggingService;

    @InjectMocks
    private HotPluggingController hotPluggingController;

    @Test
    public void testPublishHotPluggingConfig() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        PowerMockito.when(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig)).thenReturn
                (new Result<>(ResultCodeType.SUCCESS.getCode(), ResultCodeType.SUCCESS.getMessage(), true));
        Result<Boolean> result = hotPluggingController.publishHotPluggingConfig(hotPluggingConfig);
        assertResult(result, ResultCodeType.SUCCESS);
        Assert.assertTrue(result.getData());
    }

    public void assertResult(Result<Boolean> result, ResultCodeType resultCodeType) {
        Assert.assertEquals(result.getCode(), resultCodeType.getCode());
        Assert.assertEquals(result.getMessage(), resultCodeType.getMessage());
    }

    @Test
    public void testMissParam() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        PowerMockito.when(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig)).thenReturn
                (new Result<>(ResultCodeType.SUCCESS.getCode(), ResultCodeType.SUCCESS.getMessage(), true));
        Result<Boolean> result = hotPluggingController.publishHotPluggingConfig(hotPluggingConfig);
        assertResult(result, ResultCodeType.MISS_PARAM);
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        result = hotPluggingController.publishHotPluggingConfig(hotPluggingConfig);
        assertResult(result, ResultCodeType.MISS_PARAM);
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        assertResult(result, ResultCodeType.MISS_PARAM);
    }
}
