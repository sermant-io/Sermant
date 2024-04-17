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

import com.huawei.flowcontrol.common.cache.Timed;

import java.util.Map;

/**
 * request-information
 *
 * @author zhouss
 * @since 2022-01-22
 */
public interface RequestEntity extends Timed {
    /**
     * request path
     *
     * @return request path
     */
    String getApiPath();

    /**
     * get request header
     *
     * @return request header
     */
    Map<String, String> getHeaders();

    /**
     * get request method
     *
     * @return method type
     */
    String getMethod();

    /**
     * service name
     *
     * @return service name
     */
    String getServiceName();

    /**
     * request direction
     *
     * @return request direction
     */
    RequestType getRequestType();

    /**
     * the equal method must be implemented
     *
     * @param obj comparative object
     * @return whether it is equal or not
     */
    @Override
    boolean equals(Object obj);

    /**
     * the hashCode encoding must be implemented
     *
     * @return hashcode
     */
    @Override
    int hashCode();

    /**
     * Request type, marking the direction of the request, client request or server request
     *
     * @since 2022-07-20
     */
    enum RequestType {
        /**
         * client requests that is outgoing requests
         */
        CLIENT,

        /**
         * server side requests that is incoming requests
         */
        SERVER,

        /**
         * all processable
         */
        BOTH
    }
}
