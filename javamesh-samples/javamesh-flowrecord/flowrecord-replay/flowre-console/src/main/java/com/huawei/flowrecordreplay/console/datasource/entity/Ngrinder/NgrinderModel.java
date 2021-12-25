/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 引流压测单接口模板
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class NgrinderModel {
    /**
     * 接口名称
     */
    private String method;

    /**
     * 接口方法
     */
    private String methodFuction;

    /**
     * httpheader
     */
    private Map<String, String> header;

    /**
     * httpparams
     */
    private Map<String, String> params;

    /**
     * httpbody
     */
    private Map<String, String> body;

    /**
     * httpurl
     */
    private String url;
}
