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

package com.huawei.javamesh.core.lubanops.bootstrap.utils;

import java.util.regex.Pattern;

/**
 * @author
 * @date 2020/11/27 11:43
 */
public class ParamCheckUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$");

    public static boolean isUrl(String url) {
        if (StringUtils.isBlank(url) || url.length() > 100) {
            return false;
        }
        if (URL_PATTERN.matcher(url).matches()) {
            return true;
        } else {
            return false;
        }
    }
}
