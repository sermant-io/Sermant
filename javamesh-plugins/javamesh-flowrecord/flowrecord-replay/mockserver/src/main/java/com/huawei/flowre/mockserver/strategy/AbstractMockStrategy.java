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
