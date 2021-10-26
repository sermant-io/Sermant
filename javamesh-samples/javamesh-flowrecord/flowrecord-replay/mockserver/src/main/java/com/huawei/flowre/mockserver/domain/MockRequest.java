/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * MockClient请求体
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-03
 */
@Getter
@Setter
public class MockRequest {
    /**
     * 通过traceId和调用上下文获取的唯一标识key，用于准确的标识一次调用
     */
    private String subCallKey;

    /**
     * 子调用的计数
     */
    private String subCallCount;

    /**
     * 录制任务的ID
     */
    private String recordJobId;

    /**
     * 支持dubbo、http、mysql、custom类型
     */
    private String mockRequestType;

    /**
     * 请求的参数
     */
    private String arguments;

    /**
     * 接口名称
     */
    private String method;
}
