/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.service;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockAction;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockRequest;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockResult;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.SelectResult;
import com.lubanops.apm.plugin.flowreplay.mockclient.httpclient.MockHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.alibaba.fastjson.JSON;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MockHttpClient.class)
public class MockResultServiceTest {
    MockResultService mockResultService = new MockResultService();

    @Before
    public void setUp(){
        SelectResult selectResult = new SelectResult();
        selectResult.setSelectContent("content");
        selectResult.setSelectClassName("className");

        MockResult mockResult = new MockResult();
        mockResult.setSubCallKey("subCallKey");
        mockResult.setMockAction(MockAction.RETURN);
        mockResult.setMockRequestType(PluginConfig.DUBBO);
        mockResult.setSelectResult(selectResult);

        MockResult badMockResult = new MockResult();
        badMockResult.setSubCallKey("subCallKey");
        badMockResult.setMockAction(MockAction.RETURN);
        badMockResult.setMockRequestType(PluginConfig.DUBBO);
        badMockResult.setSelectResult(null);
        PowerMockito.mockStatic(MockHttpClient.class);
        PowerMockito.when(MockHttpClient
                .post(Mockito.anyString(),Mockito.anyString())).thenReturn(JSON.toJSONString(mockResult))
                .thenReturn(JSON.toJSONString(badMockResult)).thenReturn("");
    }

    @Test
    public void testGetMockResult(){
        // 测试返回值正确
        MockResult result = mockResultService.getMockResult(new MockRequest());
        Assert.assertEquals(result.getMockRequestType(),PluginConfig.DUBBO);

        // 测试selectResult为null
        result = mockResultService.getMockResult(new MockRequest());
        Assert.assertEquals(result.getMockRequestType(),PluginConfig.NOTYPE);

        // 测试http请求返回为空字符串
        result = mockResultService.getMockResult(new MockRequest());
        Assert.assertEquals(result.getMockRequestType(),PluginConfig.NOTYPE);
    }
}
