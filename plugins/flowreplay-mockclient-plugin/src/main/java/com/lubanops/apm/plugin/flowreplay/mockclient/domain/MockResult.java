/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.domain;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 从mock server获取的mock结果实体
 *
 * @author luanwenfei
 * @version 0.0.1 2021-02-08
 * @since 2021-02-08
 */
@Getter
@Setter
public class MockResult {
    /**
     * 子调用key
     */
    private String subCallKey;

    /**
     * mock执行的操作
     */
    private MockAction mockAction;

    /**
     * mock的返回值和返回值类型
     */
    private SelectResult selectResult;

    /**
     * 请求的类型
     */
    private String mockRequestType;

    public MockResult() {
        this.mockRequestType = PluginConfig.NOTYPE;
    }
}
