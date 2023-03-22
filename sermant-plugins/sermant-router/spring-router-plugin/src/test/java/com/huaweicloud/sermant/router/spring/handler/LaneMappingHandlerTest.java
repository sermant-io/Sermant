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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.spring.TestSpringConfigService;
import com.huaweicloud.sermant.router.spring.service.LaneService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;

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
 * 测试LaneMappingHandler
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
     * UT执行前进行mock
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
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public LaneMappingHandlerTest() {
        handler = new LaneMappingHandler();
    }

    /**
     * 测试getRequestTag方法
     */
    @Test
    public void testGetRequestTag() {
        // 测试matchTags为null
        configService.setReturnEmptyWhenGetMatchTags(true);
        Map<String, List<String>> requestTag = handler.getRequestTag("", "", null, null);
        Assert.assertEquals(requestTag, Collections.emptyMap());

        // 测试getLane返回空
        configService.setReturnEmptyWhenGetMatchTags(false);
        laneService.setReturnEmpty(true);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("bar", Collections.singletonList("bar1"));
        headers.put("foo", Collections.singletonList("foo1"));
        requestTag = handler.getRequestTag("", "", headers, null);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // 测试getLane不为空
        configService.setReturnEmptyWhenGetMatchTags(false);
        laneService.setReturnEmpty(false);
        requestTag = handler.getRequestTag("", "", headers, null);
        Assert.assertEquals(3, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));
        Assert.assertEquals("flag1", requestTag.get("sermant-flag").get(0));
    }
}