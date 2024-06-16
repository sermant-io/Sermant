/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.service.httpserver.api;

import java.util.Map;

/**
 * Http response
 *
 * @author zwmagic
 * @since 2024-02-02
 */
public interface HttpResponse {
    /**
     * Retrieves the response status code
     *
     * @return Response status code
     */
    int getStatus();

    /**
     * Sets the response status code
     *
     * @param status Response status code
     * @return Response object
     */
    HttpResponse setStatus(int status);

    /**
     * Adds a response header
     *
     * @param name Header name
     * @param value Header value
     * @return Response object
     */
    HttpResponse addHeader(String name, String value);

    /**
     * Sets a response header
     *
     * @param name Header name
     * @param value Header value
     * @return Response object
     */
    HttpResponse setHeader(String name, String value);

    /**
     * Sets the collection of response headers
     *
     * @param headers Map of headers
     * @return Response object
     */
    HttpResponse setHeaders(Map<String, String> headers);

    /**
     * Sets the content type of the response
     *
     * @param contentType Content type
     * @return Response object
     */
    HttpResponse setContentType(String contentType);

    /**
     * Sets the content length of the response
     *
     * @param size Content length
     * @return Response object
     */
    HttpResponse setContentLength(long size);

    /**
     * Writes the response body as a byte array
     *
     * @param bytes Byte array
     */
    void writeBody(byte[] bytes);

    /**
     * Writes the response body as a string
     *
     * @param str String
     */
    void writeBody(String str);

    /**
     * Writes the response body with an exception
     *
     * @param ex Exception object
     */
    void writeBody(Throwable ex);

    /**
     * Writes the response body as a JSON string
     *
     * @param json JSON string
     */
    void writeBodyAsJson(String json);

    /**
     * Writes the response body as a JSON object
     *
     * @param obj JSON object
     */
    void writeBodyAsJson(Object obj);
}

