/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.service;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockRequest;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockResult;
import com.lubanops.apm.plugin.flowreplay.mockclient.httpclient.MockHttpClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将json结果解析为MockResult
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-08
 * @since 2021-02-08
 */
public class MockResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockResultService.class);

    /**
     * 获取Mock的返回结果
     *
     * @param mockRequest mock请求体
     * @return 返回封装后的mock结果
     */
    public MockResult getMockResult(MockRequest mockRequest) {
        String httpResponse = MockHttpClient.post(PluginConfig.mockServerUrl + "/mockserver/result",
                JSON.toJSONString(mockRequest));

        if (PluginConfig.RETURN_BLANK.equals(httpResponse)) {
            LOGGER.error("Mock server is wrong , skip mock : {}", mockRequest.getSubCallKey());
            return null;
        }

        MockResult mockResult = JSONObject.parseObject(httpResponse, MockResult.class);

        // 获取Json返回中的mockSelectResult对象
        if (mockResult.getSelectResult() != null) {
            return mockResult;
        } else {
            LOGGER.error("This mock result is wrong , skip mock : {}", mockRequest.getSubCallKey());
            return null;
        }
    }
}
