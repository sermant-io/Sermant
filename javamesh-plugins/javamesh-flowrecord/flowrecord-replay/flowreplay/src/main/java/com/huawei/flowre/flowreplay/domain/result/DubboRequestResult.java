/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain.result;

import lombok.Getter;
import lombok.Setter;

/**
 * dubbo请求的返回结果封装
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-17
 */
@Getter
@Setter
public class DubboRequestResult {
    /**
     * 响应状态
     */
    int statusCode;

    /**
     * 返回体
     */
    Object result;

    /**
     * 响应时间
     */
    long responseTime;
}
