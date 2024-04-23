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

package io.sermant.discovery.entity;

import java.net.URI;
import java.util.Map;

/**
 * Asynchronous contexts, which are used to store context information
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class HttpAsyncContext {
    /**
     * The callback parameter is subscripted
     */
    public static final int CALL_BACK_INDEX = 3;

    /**
     * HttpClient asynchronous handler to regenerate the request, replacing the URL address
     */
    private Object handler;

    /**
     * Selected instances
     */
    private ServiceInstance selectedInstance;

    /**
     * The callback is specified by the user
     */
    private Object callback;

    /**
     * Store the service name and request path
     */
    private Map<String, String> hostAndPath;

    /**
     * Method type
     */
    private String method;

    /**
     * The path of the request
     */
    private URI uri;

    /**
     * Request a domain name
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
