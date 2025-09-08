/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo;

import java.util.regex.Pattern;

/**
 * apollo tool class
 *
 * @author Chen Zhenyang
 * @since 2025-08-18
 */
public class ApolloUtils {
    private static final String BASE_VALID_REGEX = "^(?!^[./]$)[a-zA-Z0-9\\-_.&=/]+$";

    private static final String FORBIDDEN_SUFFIX_REGEX = ".*[./](json|yml|yaml|xml|properties)$";
    private static final Pattern BASE_VALID_PATTERN = Pattern.compile(BASE_VALID_REGEX);
    private static final Pattern FORBIDDEN_SUFFIX_PATTERN = Pattern.compile(FORBIDDEN_SUFFIX_REGEX);

    private ApolloUtils() {
    }

    /**
     * Check whether the group name is valid
     * 1. supports only English and digit characters and three special symbols ('.','-','_')
     * 2. not supports single "." or "/", and it cannot end with ".json", ".yml", ".yaml",
     * ".xml", or ".properties".
     *
     * @param namespace group name
     * @return true: valid; false: invalid
     */
    public static boolean isValidNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return false;
        }
        return BASE_VALID_PATTERN.matcher(namespace).matches()
                && !FORBIDDEN_SUFFIX_PATTERN.matcher(namespace).matches();
    }

    /**
     * Rebuild the valid group name.
     *
     * @param group group name
     * @return valid group
     */
    public static String rebuildGroup(String group) {
        return group.replace('=', '_').replace('&', '-').replace('/', '.');
    }

    /**
     * convert the group name to apollo group
     *
     * @param group valid apollo name
     * @return group name
     */
    public static String convertGroup(String group) {
        return group.replace('_', '=').replace('-', '&').replace('.', '/');
    }
}
