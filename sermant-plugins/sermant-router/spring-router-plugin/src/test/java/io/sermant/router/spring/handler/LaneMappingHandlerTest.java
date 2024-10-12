/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.handler;

import io.sermant.core.service.ServiceManager;
import io.sermant.router.spring.TestSpringConfigService;
import io.sermant.router.spring.service.LaneService;
import io.sermant.router.spring.service.SpringConfigService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test LaneMappingHandler
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class LaneMappingHandlerTest {
    private static MockedStatic<ServiceManager> mockServiceManager;

    private static TestLaneService laneService;

    private static TestSpringConfigService configService;

    private final LaneMappingHandler handler;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        laneService = new TestLaneService();
        mockServiceManager.when(() -> ServiceManager.getService(LaneService.class))
                .thenReturn(laneService);
        configService = new TestSpringConfigService();
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class))
                .thenReturn(configService);
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public LaneMappingHandlerTest() {
        handler = new LaneMappingHandler();
    }

    /**
     * Test the getRequestTag method
     */
    @Test
    public void testGetRequestTag() {
        // Test matchTags as null
        configService.setReturnEmptyWhenGetMatchTags(true);
        Map<String, List<String>> requestTag = handler.getRequestTag("", "", null, null);
        Assert.assertEquals(requestTag, Collections.emptyMap());

        // Test getLane returns null
        configService.setReturnEmptyWhenGetMatchTags(false);
        laneService.setReturnEmpty(true);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("bar", Collections.singletonList("bar1"));
        headers.put("foo", Collections.singletonList("foo1"));
        requestTag = handler.getRequestTag("", "", headers, null);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // Test getLane is not empty
        configService.setReturnEmptyWhenGetMatchTags(false);
        laneService.setReturnEmpty(false);
        requestTag = handler.getRequestTag("", "", headers, null);
        Assert.assertEquals(3, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));
        Assert.assertEquals("flag1", requestTag.get("sermant-flag").get(0));
    }
}
