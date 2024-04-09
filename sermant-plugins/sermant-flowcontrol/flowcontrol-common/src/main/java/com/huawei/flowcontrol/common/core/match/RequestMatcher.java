/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Based on org/apache/servicecomb/governance/marker/Matcher.java from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.core.match;

import com.huawei.flowcontrol.common.core.match.operator.Operator;
import com.huawei.flowcontrol.common.core.match.operator.OperatorManager;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.common.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * single request matching
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class RequestMatcher implements Matcher {
    /**
     * scenario name
     */
    private String name;

    /**
     * request header matching
     */
    private Map<String, RawOperator> headers;

    /**
     * request path
     */
    private RawOperator apiPath;

    /**
     * methodType： GET, POST, PUT, DELETE, HEAD, PATCH, OPTIONS;
     */
    private List<String> method;

    /**
     * matching target service name
     */
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

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
     * if it matches
     *
     * the matching rules are as follows:
     * 1.If the requested method is not included, it will not pass
     * 2.the path of the request must match
     * 3.the request headers match exactly
     *
     * @param requestEntity requestBody
     * @return if it matches
     */
    @Override
    public boolean match(RequestEntity requestEntity) {
        if (!isMethodMatch(requestEntity.getMethod())) {
            return false;
        }
        if (!isServiceNameMatch(requestEntity.getServiceName())) {
            return false;
        }
        if (!isHeadersMatch(requestEntity.getHeaders())) {
            return false;
        }
        return isApiPathMatch(requestEntity.getApiPath());
    }

    private boolean isServiceNameMatch(String targetServiceName) {
        if (this.serviceName == null) {
            return true;
        }
        return this.serviceName.equals(targetServiceName);
    }

    private boolean isHeadersMatch(Map<String, String> requestHeaders) {
        if (this.headers == null) {
            return true;
        }

        // matching request header
        for (Map.Entry<String, RawOperator> entry : this.headers.entrySet()) {
            final String headerValue = requestHeaders.get(entry.getKey());
            if (StringUtils.isEmpty(headerValue)) {
                return false;
            }
            if (!operatorMatch(headerValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean isApiPathMatch(String api) {
        if (this.apiPath == null) {
            return true;
        }
        return operatorMatch(api, this.apiPath);
    }

    private boolean isMethodMatch(String requestMethod) {
        if (this.method == null) {
            return true;
        }
        return this.method.contains(requestMethod);
    }

    private boolean operatorMatch(String target, RawOperator operator) {
        if (operator == null || operator.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : operator.entrySet()) {
            final Operator matchOperator = OperatorManager.INSTANCE.getOperator(entry.getKey());
            if (matchOperator == null) {
                // no relevant matcher, data error！
                return false;
            }
            if (!matchOperator.match(target, String.valueOf(entry.getValue()))) {
                // condition all match
                return false;
            }
        }
        return true;
    }
}
