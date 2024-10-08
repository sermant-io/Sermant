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
 * Constants Class
 *
 * @author zwmagic
 * @since 2024-02-02
 */
public final class Constants {
    /**
     * Default character encoding
     */
    public static final String DEFAULT_ENCODE = "UTF-8";

    /**
     * Header field name indicating the MIME type and encoding of the HTTP message body
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Header field name indicating the size of the HTTP message body in bytes
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * HTTP path separator
     */
    public static final String HTTP_PATH_DIVIDER = "/";

    /**
     * Private constructor to prevent instantiation from outside
     */
    private Constants() {
    }
}
