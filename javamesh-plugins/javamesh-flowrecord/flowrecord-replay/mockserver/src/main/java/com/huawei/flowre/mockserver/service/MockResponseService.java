/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowre.mockserver.service;

import com.huawei.flowre.mockserver.config.MSConst;
import com.huawei.flowre.mockserver.domain.MockAction;
import com.huawei.flowre.mockserver.domain.MockRequest;
import com.huawei.flowre.mockserver.domain.MockRequestType;
import com.huawei.flowre.mockserver.domain.MockResult;
import com.huawei.flowre.mockserver.strategy.DefaultMockStrategy;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * service类 controller接收到mock request通过mock service进行处理并封装返回mock结果
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-07
 */
@Service
public class MockResponseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockResponseService.class);
    @Autowired
    DefaultMockStrategy defaultMockStrategy;

    @Autowired
    CuratorFramework zkClient;

    /**
     * 获取MockResponse
     *
     * @param mockRequest mock请求
     * @return MockResponse 从es获取结果并进行封装
     */
    public MockResult getResponse(MockRequest mockRequest) {
        MockResult mockResult = new MockResult();
        MockRequestType mockRequestType = isMockRequestType(mockRequest.getMockRequestType());
        mockResult.setSubCallKey(mockRequest.getSubCallKey());

        // 不支持的mock类型
        if (mockRequestType.equals(MockRequestType.NOTYPE)) {
            mockResult.setMockRequestType(MockRequestType.NOTYPE.getName());
            mockResult.setMockAction(MockAction.SKIP);
            return mockResult;
        }

        // 指定跳过的接口
        if (isSkipMethod(mockRequest.getMethod())) {
            mockResult.setMockRequestType(mockRequest.getMockRequestType());
            mockResult.setMockAction(MockAction.SKIP);
            return mockResult;
        }
        return defaultMockStrategy.assemble(mockRequest, mockRequestType);
    }

    /**
     * 判断是否支持这种请求类型的Mock
     *
     * @param requestType 发来请求的服务类型
     * @return MockRequestType 判断是否支持该种类型的Mock
     */
    public MockRequestType isMockRequestType(String requestType) {
        MockRequestType mockRequestType;
        switch (requestType) {
            case "dubbo": {
                mockRequestType = MockRequestType.DUBBO;
                break;
            }
            case "http": {
                mockRequestType = MockRequestType.HTTP;
                break;
            }
            case "mysql": {
                mockRequestType = MockRequestType.MYSQL;
                break;
            }
            case "redis": {
                mockRequestType = MockRequestType.REDIS;
                break;
            }
            case "custom": {
                mockRequestType = MockRequestType.CUSTOM;
                break;
            }
            default: {
                mockRequestType = MockRequestType.NOTYPE;
            }
        }
        return mockRequestType;
    }

    /**
     * 检查是否忽略该方法
     *
     * @param method 方法名
     * @return boolean
     */
    public boolean isSkipMethod(String method) {
        Stat stat = null;
        try {
            stat = zkClient.checkExists().forPath(MSConst.SKIP_METHOD + method);
        } catch (Exception exception) {
            LOGGER.error("Get skip method error : {}", exception.getMessage());
        }
        return stat == null;
    }
}
