/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */
package com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * HTTP回放字段内容
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-12-10
 */
@Getter
@Setter
public class HttpRequestEntity {
    /**
     * 请求url
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 请求头
     */
    private Map<String, String> headMap;

    /**
     * 请求json字符串
     */
    private String httpRequestBody;
}

