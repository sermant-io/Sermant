/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
