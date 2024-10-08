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

import io.sermant.core.service.httpserver.exception.HttpServerException;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * HTTP Request
 *
 * @author zwmagic
 * @since 2024-02-02
 */
public interface HttpRequest {
    /**
     * Retrieves the URI
     *
     * @return URI object
     */
    URI getUri();

    /**
     * Resolved request path
     *
     * @return request path
     */
    String getPath();

    /**
     * Original request path
     *
     * @return request path
     */
    String getOriginalPath();

    /**
     * HTTP method.
     *
     * @return HttpMethod such as GET, POST, etc.
     */
    String getMethod();

    /**
     * Gets the content type
     *
     * @return content type
     */
    String getContentType();

    /**
     * Retrieves the IP address
     *
     * @return IP address
     */
    String getIp();

    /**
     * Gets the first value of a specified header
     *
     * @param name header key
     * @return first value of the named header
     */
    String getFirstHeader(String name);

    /**
     * Retrieves the first value of a specified header with a default value
     *
     * @param name header key
     * @param defaultValue default value when value is null
     * @return first value of the named header or default value
     */
    String getFirstHeader(String name, String defaultValue);

    /**
     * Retrieves all headers
     *
     * @return a Map containing all headers, where keys are header names and values are lists of header values
     */
    Map<String, List<String>> getHeaders();

    /**
     * Query parameter by specified name
     * <pre>
     *     e.g., http://127.0.0.1:8080/api/v1/test?k1=v1&k2=v2, params are k1=v1&k2=v2
     * </pre>
     *
     * @param name parameter name
     * @return value of the named parameter
     */
    String getParam(String name);

    /**
     * Query parameter by specified name with a default value
     * <pre>
     *     e.g., http://127.0.0.1:8080/api/v1/test?k1=v1&k2=v2, params are k1=v1&k2=v2
     * </pre>
     *
     * @param name parameter name
     * @param defaultValue default value when value is null
     * @return value of the named parameter or default value
     */
    String getParam(String name, String defaultValue);

    /**
     * All query parameters
     * <pre>
     *     e.g., http://127.0.0.1:8080/api/v1/test?k1=v1&k2=v2, params are k1=v1&k2=v2
     * </pre>
     *
     * @return map of query parameters
     */
    Map<String, String> getParams();

    /**
     * Retrieves the request body
     *
     * @return request body content
     * @throws HttpServerException if an HTTP server exception occurs
     */
    String getBody() throws HttpServerException;

    /**
     * Parses an object of the specified type from the request body
     *
     * @param <T> generic type
     * @param clazz class to parse
     * @return parsed object
     * @throws HttpServerException if an HTTP server exception occurs
     */
    <T> T getBody(Class<T> clazz) throws HttpServerException;

    /**
     * Retrieves the request body with a specified charset
     *
     * @param charset character set
     * @return request body content
     * @throws HttpServerException if an HTTP server exception occurs
     */
    String getBody(Charset charset) throws HttpServerException;

    /**
     * Converts the request body to a byte array
     *
     * @return byte array of the request body
     * @throws HttpServerException if an HTTP server exception occurs
     */
    byte[] getBodyAsBytes() throws HttpServerException;

    /**
     * Parses a list of objects of the specified type from the request body
     *
     * @param <T> generic type
     * @param clazz class to parse
     * @return list of parsed objects
     * @throws HttpServerException if an HTTP server exception occurs
     */
    <T> List<T> getBodyAsList(Class<T> clazz) throws HttpServerException;

    /**
     * Retrieves the request body as an input stream
     *
     * @return input stream of the request body
     */
    InputStream getBodyAsStream();
}
