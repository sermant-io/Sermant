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

package io.sermant.core.service.httpserver.exception;

/**
 * HTTP Server Exception
 *
 * @author zwmagic
 * @since 2024-02-01
 */
public class HttpServerException extends RuntimeException {
    /**
     * Private member variable, represents the HTTP status code
     */
    private final int status;

    /**
     * Constructor for creating an HttpServerException instance
     *
     * @param status HTTP status code
     * @param message Exception message
     */
    public HttpServerException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructor for creating an HttpServerException instance with cause
     *
     * @param status HTTP status code
     * @param message Exception message
     * @param cause Original throwable causing the exception
     */
    public HttpServerException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Constructor for creating an HttpServerException instance with cause but no explicit message
     *
     * @param status HTTP status code
     * @param cause Original throwable causing the exception
     */
    public HttpServerException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    /**
     * Retrieves the HTTP status code
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }
}
