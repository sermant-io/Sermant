/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.dubbo.strategy.type;

import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.utils.ReflectUtils;
import io.sermant.router.dubbo.strategy.TypeStrategy;

import java.util.Optional;

/**
 * entity matching policies
 *
 * @author provenceee
 * @since 2021-10-13
 */
public class ObjectTypeStrategy extends TypeStrategy {
    private static final int INDEX_BETWEEN_LOWERCASE_LETTER_AND_UPPERCASE_LETTER = 32;

    private static final String GET_METHOD_NAME_PREFIX = "get";

    @Override
    public Optional<String> getValue(Object arg, String type) {
        String methodName = getMethodNameByFieldName(getKey(type));
        return Optional.ofNullable(ReflectUtils.invokeWithNoneParameterAndReturnString(arg, methodName));
    }

    @Override
    public boolean isMatch(String type) {
        if (StringUtils.isBlank(type) || !type.startsWith(".")) {
            return false;
        }
        return !type.contains("(") && !type.contains(")") && !type.contains("[") && !type.contains("]");
    }

    @Override
    public String getBeginFlag() {
        return ".";
    }

    @Override
    public String getEndFlag() {
        return "";
    }

    private String getMethodNameByFieldName(String fieldName) {
        char[] chars = fieldName.toCharArray();
        if (Character.getType(chars[0]) == Character.LOWERCASE_LETTER) {
            // Capitalize the first letter
            chars[0] -= INDEX_BETWEEN_LOWERCASE_LETTER_AND_UPPERCASE_LETTER;
        }

        // Field name is xxx, converted to method name getXxx
        return GET_METHOD_NAME_PREFIX + String.valueOf(chars);
    }
}