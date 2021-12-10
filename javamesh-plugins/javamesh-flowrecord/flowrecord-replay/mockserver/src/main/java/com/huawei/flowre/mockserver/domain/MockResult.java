/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowre.mockserver.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 封装后的Mock结果 ，用于返回给mock client
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
@Getter
@Setter
public class MockResult {
    /**
     * mock结果的唯一标识
     */
    private String subCallKey;
    /**
     * 返回mock的类型
     */
    private String mockRequestType;
    /**
     * mock返回值的
     */
    private SelectResult selectResult;
    /**
     * mock操作类型 Return Throw Skip
     */
    private MockAction mockAction;

    public MockResult(){
        this.selectResult = new SelectResult();
    }
}
