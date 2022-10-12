/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.discovery.entity;

import java.net.URI;
import java.util.Map;

/**
 * 异步上下文, 用于存储上下文信息
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class HttpAsyncContext {
    /**
     * 回调参数下标
     */
    public static final int CALL_BACK_INDEX = 3;

    /**
     * httpclient异步处理器, 用于重新生成请求, 替换url地址
     */
    private Object handler;

    /**
     * 选择的实例
     */
    private ServiceInstance selectedInstance;

    /**
     * 用户指定回调
     */
    private Object callback;

    /**
     * 存储服务名与请求路径
     */
    private Map<String, String> hostAndPath;

    /**
     * 方法类型
     */
    private String method;

    /**
     * 请求路径
     */
    private URI uri;

    /**
     * 请求域名
     */
    private String originHostName;

    public String getOriginHostName() {
        return originHostName;
    }

    public void setOriginHostName(String originHostName) {
        this.originHostName = originHostName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getCallback() {
        return callback;
    }

    public void setCallback(Object callback) {
        this.callback = callback;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public ServiceInstance getSelectedInstance() {
        return selectedInstance;
    }

    public void setSelectedInstance(ServiceInstance selectedInstance) {
        this.selectedInstance = selectedInstance;
    }

    public Map<String, String> getHostAndPath() {
        return hostAndPath;
    }

    public void setHostAndPath(Map<String, String> hostAndPath) {
        this.hostAndPath = hostAndPath;
    }
}
