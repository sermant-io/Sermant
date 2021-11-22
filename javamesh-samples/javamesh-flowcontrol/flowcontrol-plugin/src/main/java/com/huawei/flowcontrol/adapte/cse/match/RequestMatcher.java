/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import java.util.List;
import java.util.Map;

/**
 * 单个请求匹配
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RequestMatcher implements Matcher {
    /**
     * 场景名
     */
    private String name;

    /**
     * 请求头匹配
     */
    private Map<String, RawOperator> headers;

    /**
     * 请求路径
     */
    private RawOperator apiPath;

    /**
     * 方法类型
     *     GET,
     *     POST,
     *     PUT,
     *     DELETE,
     *     HEAD,
     *     PATCH,
     *     OPTIONS;
     */
    private List<String> method;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, RawOperator> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, RawOperator> headers) {
        this.headers = headers;
    }

    public RawOperator getApiPath() {
        return apiPath;
    }

    public void setApiPath(RawOperator apiPath) {
        this.apiPath = apiPath;
    }

    public List<String> getMethod() {
        return method;
    }

    public void setMethod(List<String> method) {
        this.method = method;
    }
}
