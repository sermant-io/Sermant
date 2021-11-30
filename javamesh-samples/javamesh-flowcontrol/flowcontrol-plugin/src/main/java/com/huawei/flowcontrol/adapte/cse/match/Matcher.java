/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import com.huawei.apm.core.lubanops.integration.enums.HttpMethod;

import java.util.Map;

/**
 * 匹配器
 *
 * @author zhouss
 * @since 2021-11-16
 */
public interface Matcher {
    /**
     * 匹配
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param method 方法类型
     * @return 是否匹配
     */
    boolean match(String url, Map<String, String> headers, String method);
}
