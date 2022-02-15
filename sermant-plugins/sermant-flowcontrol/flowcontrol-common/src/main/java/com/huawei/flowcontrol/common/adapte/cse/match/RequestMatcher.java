/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on org/apache/servicecomb/governance/marker/Matcher.java from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.match;

import com.huawei.flowcontrol.common.adapte.cse.match.operator.Operator;
import com.huawei.flowcontrol.common.adapte.cse.match.operator.OperatorManager;

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

    /**
     * 是否匹配
     *
     * 匹配规则如下:
     * 1.请求的方法未被包含在内，则不通过
     * 2.请求的路劲必须匹配
     * 3.请求头完全匹配
     *
     * @param url 请求地址
     * @param requestHeaders 请求头
     * @param requestMethod 请求方法
     * @return 是否匹配
     */
    @Override
    public boolean match(String url, Map<String, String> requestHeaders, String requestMethod) {
        if (isApiPathNotMatch(url) || isMethodNotMatch(requestMethod)) {
            return false;
        }
        if (this.headers == null) {
            return true;
        }

        // 匹配请求头
        for (Map.Entry<String, RawOperator> entry : this.headers.entrySet()) {
            final String headerValue = requestHeaders.get(entry.getKey());
            if (headerValue == null) {
                return false;
            }
            if (!operatorMatch(headerValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean isApiPathNotMatch(String url) {
        return this.apiPath != null && !operatorMatch(url, this.apiPath);
    }

    private boolean isMethodNotMatch(String requestMethod) {
        return this.method != null && !this.method.contains(requestMethod);
    }

    private boolean operatorMatch(String target, RawOperator operator) {
        if (operator == null || operator.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : operator.entrySet()) {
            final Operator matchOperator = OperatorManager.INSTANCE.getOperator(entry.getKey());
            if (matchOperator == null) {
                // 无相关匹配器，数据错误！
                return false;
            }
            if (!matchOperator.match(target, entry.getValue())) {
                // 条件全部匹配
                return false;
            }
        }
        return true;
    }
}
