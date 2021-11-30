/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.strategy;

import com.huawei.flowre.mockserver.domain.MockAction;
import com.huawei.flowre.mockserver.domain.MockRequest;
import com.huawei.flowre.mockserver.domain.MockRequestType;
import com.huawei.flowre.mockserver.domain.MockResult;
import com.huawei.flowre.mockserver.domain.SelectResult;

/**
 * mock策略抽象类，select方法由子类实现，assemble对select的结果进行封装
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
public abstract class AbstractMockStrategy {
    public abstract SelectResult selectMockResult(MockRequest mockRequest);

    public MockResult assemble(MockRequest mockRequest, MockRequestType mockRequestType) {
        MockResult mockResult = new MockResult();
        SelectResult selectResult = selectMockResult(mockRequest);
        mockResult.setSubCallKey(mockRequest.getSubCallKey());
        mockResult.setSelectResult(selectResult);
        mockResult.setMockRequestType(mockRequestType.getName());
        mockResult.setMockAction(MockAction.RETURN);
        return mockResult;
    }
}
