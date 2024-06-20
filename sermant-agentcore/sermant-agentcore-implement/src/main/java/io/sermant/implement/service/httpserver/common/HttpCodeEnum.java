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

package io.sermant.implement.service.httpserver.common;

/**
 * HTTP Status Code Enumeration, defining common HTTP response codes and their respective messages.
 *
 * @author zwmagic
 * @since 2024-02-04
 */
public enum HttpCodeEnum {
    /**
     * Successful request
     */
    SUCCESS(200, "SUCCESS"),

    /**
     * Client request is erroneous, server unable to understand
     */
    BAD_REQUEST(400, "BAD REQUEST"),

    /**
     * Request denied by the server, no permission
     */
    FORBIDDEN(403, "FORBIDDEN"),

    /**
     * Request requires user authentication, i.e., unauthorized
     */
    UNAUTHORIZED(401, "UNAUTHORIZED"),

    /**
     * Server cannot find the requested resource
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * Request method is not allowed
     */
    METHOD_NOT_ALLOWED(405, "METHOD NOT ALLOWED"),

    /**
     * Server internal error, unable to fulfill the request
     */
    SERVER_ERROR(500, "SERVER ERROR");

    /**
     * HTTP status code
     */
    private final int code;

    /**
     * Text message corresponding to the status code
     */
    private final String message;

    /**
     * Constructor to initialize enum instance
     *
     * @param code Status code
     * @param message Message corresponding to the status code
     */
    HttpCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the HTTP status code
     *
     * @return Status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the text message corresponding to the status code
     *
     * @return Message
     */
    public String getMessage() {
        return message;
    }
}
