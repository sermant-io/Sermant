/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain.result;

import lombok.Getter;
import lombok.Setter;

/**
 * http请求的返回数据封装
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-06
 */
@Getter
@Setter
public class HttpRequestResult {
    /**
     * 状态码
     */
    int statusCode;

    /**
     * 返回体
     */
    String responseBody;

    /**
     * 响应时间
     */
    long responseTime;
}
