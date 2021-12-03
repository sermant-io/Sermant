/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.util;


import com.huawei.javamesh.core.common.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.huawei.javamesh.core.util.PathUtils.normalize;
import static com.huawei.javamesh.core.util.StringUtils.SLASH;
import static java.util.Arrays.asList;

public class URIUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    public static String getParameter(URI uri, String name, String defaultValue) {
        if ( uri == null )
            return defaultValue;

        String query = uri.getQuery();
        if ( query == null )
            return defaultValue;

        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.warning(e.getMessage());
                return defaultValue;
            }
        }

        String rs = query_pairs.get(name);
        if ( rs == null )
            return defaultValue;
        else
            return rs;
    }

    public static int getParameter(URI uri, String name, int defaultValue) {
        String rs = getParameter(uri, name, null);
        if ( rs == null )
            return defaultValue;
        else
        {
            return Integer.parseInt(rs);
        }
    }

    public static long getParameter(URI uri, String name, long defaultValue) {
        String rs = getParameter(uri, name, null);
        if ( rs == null )
            return defaultValue;
        else
        {
            return Long.parseLong(rs);
        }
    }

    static String buildPath(String rootPath, String... subPaths) {

        Set<String> paths = new LinkedHashSet<>();
        paths.add(rootPath);
        paths.addAll(asList(subPaths));

        return normalize(paths.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(SLASH)));
    }

}
