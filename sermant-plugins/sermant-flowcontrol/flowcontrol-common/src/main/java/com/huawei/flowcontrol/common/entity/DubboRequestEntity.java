/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.entity;

import java.util.Collections;
import java.util.Map;

/**
 * dubbo request body
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class DubboRequestEntity extends AbstractRequestEntity {
    /**
     * dubbo method matching type
     */
    public static final String METHOD = "POST";

    private String apiPath;

    private Map<String, String> attachments;

    /**
     * Whether the interface is a dubbo generalization interface
     */
    private final boolean isGeneric;

    /**
     * construct the dubbo request body
     *
     * @param apiPath request path
     * @param attachments extra parameter
     * @param requestType request type
     * @param serviceName service name
     */
    public DubboRequestEntity(String apiPath, Map<String, String> attachments, RequestType requestType,
            String serviceName) {
        this(apiPath, attachments, requestType, serviceName, false);
    }

    /**
     * construct the dubbo request body
     *
     * @param apiPath request path
     * @param attachments extra parameter
     * @param requestType request type
     * @param serviceName service name
     * @param isGeneric Whether the interface is a generalization interface
     */
    public DubboRequestEntity(String apiPath, Map<String, String> attachments, RequestType requestType,
            String serviceName, boolean isGeneric) {
        this.apiPath = apiPath;
        this.attachments = Collections.unmodifiableMap(attachments);
        this.isGeneric = isGeneric;
        setRequestType(requestType);
        setServiceName(serviceName);
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    @Override
    public String getApiPath() {
        return apiPath;
    }

    @Override
    public Map<String, String> getHeaders() {
        return this.attachments;
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}
