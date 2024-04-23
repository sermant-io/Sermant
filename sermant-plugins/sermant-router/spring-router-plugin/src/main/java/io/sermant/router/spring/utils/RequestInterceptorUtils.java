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

package io.sermant.router.spring.utils;

import io.sermant.core.common.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Parse URL parameters and build utility classes related to public methods
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class RequestInterceptorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RequestInterceptorUtils() {
    }

    /**
     * Format the URI
     *
     * @param uri Destination URI
     * @return URI
     */
    public static Optional<URI> formatUri(String uri) {
        if (!isValidUrl(uri)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URI(uri));
        } catch (URISyntaxException e) {
            LOGGER.fine(String.format(Locale.ENGLISH, "%s is not valid uri!", uri));
            return Optional.empty();
        }
    }

    private static boolean isValidUrl(String url) {
        final String lowerCaseUrl = url.toLowerCase(Locale.ROOT);
        return lowerCaseUrl.startsWith("http") || lowerCaseUrl.startsWith("https");
    }
}
