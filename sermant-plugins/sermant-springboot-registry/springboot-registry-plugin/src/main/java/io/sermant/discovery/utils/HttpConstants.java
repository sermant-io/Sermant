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

package io.sermant.discovery.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parameter label class
 *
 * @author chengyouling
 * @since 2022-09-14
 */
public class HttpConstants {
    /**
     * domain name
     */
    public static final String HTTP_URI_HOST = "host";

    /**
     * The name of the service resolved from the URL
     */
    public static final String HTTP_URI_SERVICE = "serviceName";

    /**
     * Port
     */
    public static final String HTTP_URI_PORT = "port";

    /**
     * Path
     */
    public static final String HTTP_URI_PATH = "path";

    /**
     * Double slashes
     */
    public static final String HTTP_URL_DOUBLE_SLASH = "://";

    /**
     * Profanity
     */
    public static final String HTTP_URL_COLON = ":";

    /**
     * question mark
     */
    public static final String HTTP_URL_UNKNOWN = "?";

    /**
     * Single slash
     */
    public static final char HTTP_URL_SINGLE_SLASH = '/';

    /**
     * Empty strings
     */
    public static final String EMPTY_STR = "";

    /**
     * protocol
     */
    public static final String HTTP_URL_SCHEME = "scheme";

    /**
     * Time format
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private HttpConstants() {
    }

    /**
     * Get the current time
     *
     * @return 时间
     */
    public static String currentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DATE_TIME_FORMATTER);
    }
}
