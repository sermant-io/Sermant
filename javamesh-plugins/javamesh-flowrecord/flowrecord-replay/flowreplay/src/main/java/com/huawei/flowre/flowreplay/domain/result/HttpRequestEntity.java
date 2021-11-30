/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain.result;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * http请求的入参数据封装
 *
 * @author lihongjiang
 * @version 0.0.1
 * @since 2021-08-16
 */
@Getter
@Setter
public class HttpRequestEntity {
    private String url;

    private String method;

    private Map<String, String> headMap;

    private String httpRequestBody;
}
