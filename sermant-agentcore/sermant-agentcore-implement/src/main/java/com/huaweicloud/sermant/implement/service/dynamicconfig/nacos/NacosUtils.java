/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.nacos;

import java.util.regex.Pattern;

/**
 * Nacos tool class
 *
 * @author tangle
 * @since 2023-08-30
 */
public class NacosUtils {
    /**
     * The regular expression is used to legalize the group name
     */
    public static final String ALLOWED_CHARS = "[a-zA-Z.=&:\\-_/]+";

    private static final Pattern PATTERN = Pattern.compile(ALLOWED_CHARS);

    private NacosUtils() {
    }

    /**
     * Check whether the group name is valid
     *
     * @param group group name
     * @return true: valid; false: invalid
     */
    public static boolean isValidGroupName(String group) {
        return PATTERN.matcher(group).matches();
    }

    /**
     * Rebuild the valid group name. Nacos's group supports only English characters and four special symbols ('.',
     * ':','-','_'). Here '.' replaces '/' to represent the parent-child node hierarchy; ':' Replace '='; '&' replaces
     * '_'.
     *
     * @param group group name
     * @return valid group
     */
    public static String reBuildGroup(String group) {
        return group.replace('=', ':').replace('&', '_').replace('/', '.');
    }
}
