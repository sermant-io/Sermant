/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 获取mock数据的请求体封装
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-14
 */
@Getter
@Setter
public class MockRequest {
    /**
     * 通过traceId、接口名、请求参数类型和请求值生成的标识
     */
    String subCallKey;

    /**
     * subCall的调用次数
     */
    int subCallCount;

    /**
     * 录制任务的id 用于标识查找mock结果的index
     */
    String recordJobId;

    /**
     * 请求的类型
     */
    String mockRequestType;

    /**
     * 请求的参数
     */
    String arguments;

    /**
     * 接口名称
     */
    String method;

    public MockRequest(String subCallKey, int subCallCount,
                       String recordJobId, String mockRequestType,
                       String arguments, String method) {
        this.subCallKey = subCallKey;
        this.subCallCount = subCallCount;
        this.recordJobId = recordJobId;
        this.mockRequestType = mockRequestType;
        this.arguments = arguments;
        this.method = method;
    }

    public MockRequest() {
    }
}
