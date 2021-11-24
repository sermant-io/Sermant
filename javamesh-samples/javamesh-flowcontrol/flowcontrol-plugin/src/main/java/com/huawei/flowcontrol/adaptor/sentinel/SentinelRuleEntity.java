/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adaptor.sentinel;

import com.huawei.apm.core.lubanops.integration.enums.HttpMethod;

import java.util.Map;

/**
 * 规则实体
 *
 * @author zhouss
 * @since 2021-11-22
 */
public interface SentinelRuleEntity {
    /**
     * 规则唯一资源名名
     *
     * @return resource
     */
    String getResource();

    /**
     * 是否匹配
     *
     * @param url     请求路径
     * @param headers 请求头
     * @param method  方法类型
     * @return boolean
     */
    boolean match(String url, Map<String, String> headers, HttpMethod method);

}
