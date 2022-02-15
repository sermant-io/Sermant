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
 * Based on org/apache/servicecomb/governance/marker/GovernanceRequest.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.entity;

import java.util.Map;

/**
 * 匹配请求
 *
 * @author zhouss
 * @since 2021-11-24
 */
public class CseMatchRequest {
    private String apiPath;

    private Map<String, String> headers;

    private String httpMethod;

    public CseMatchRequest(String apiPath, Map<String, String> headers, String httpMethod) {
        this.apiPath = apiPath;
        this.headers = headers;
        this.httpMethod = httpMethod;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
