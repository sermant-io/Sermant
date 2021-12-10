/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.bootstrap.commons;

import java.util.regex.Pattern;

import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;

/**
 * @author
 * @date 2021/2/4 11:29
 */
public class ValidatorUtil {

    public static void validate(String key, String value, boolean notNull, int length, String regex) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException(String.format("[PARAMETER CHECK] input key[{%s}] is blank.", key));
        }
        if (StringUtils.isBlank(value)) {
            if (notNull) {
                throw new IllegalArgumentException(
                        String.format("[PARAMETER CHECK]value of key[{%s}] can't be empty.", key));
            } else {
                return;
            }
        }
        if (value.length() > length) {
            throw new IllegalArgumentException(
                    String.format("[PARAMETER CHECK] key[{%s}]value of key[{%s}] must contain less than 64 characters.",
                            key, key));
        }
        if (!StringUtils.isBlank(regex)) {
            if (!Pattern.matches(regex, value)) {
                throw new IllegalArgumentException(
                        String.format("[PARAMETER CHECK] key[{%s}] value[%s] contain illegal characters.", key, value));
            }
        }
    }

}
