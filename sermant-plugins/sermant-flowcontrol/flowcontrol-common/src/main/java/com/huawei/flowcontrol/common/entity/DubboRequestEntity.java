/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 * dubbo请求体
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class DubboRequestEntity extends AbstractRequestEntity {
    /**
     * dubbo方法匹配类型
     */
    public static final String METHOD = "POST";

    private String apiPath;

    private Map<String, String> attachments;

    /**
     * 是否为dubbo泛化接口
     */
    private final boolean isGeneric;

    /**
     * 构造dubbo请求体
     *
     * @param apiPath 请求路径
     * @param attachments 额外参数
     * @param requestType 请求类型
     * @param serviceName 服务名
     */
    public DubboRequestEntity(String apiPath, Map<String, String> attachments, RequestType requestType,
            String serviceName) {
        this(apiPath, attachments, requestType, serviceName, false);
    }

    /**
     * 构造dubbo请求体
     *
     * @param apiPath 请求路径
     * @param attachments 额外参数
     * @param requestType 请求类型
     * @param serviceName 服务名
     * @param isGeneric 是否为泛化接口
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
