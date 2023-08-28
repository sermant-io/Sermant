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
 * Nacos配置工具类
 *
 * @author tangle
 * @since 2023-08-30
 */
public class NacosUtils {
    /**
     * 正则表达式，用于group名称合法化
     */
    public static final String ALLOWED_CHARS = "[a-zA-Z.=&:\\-_/]+";

    private static final Pattern PATTERN = Pattern.compile(ALLOWED_CHARS);

    private NacosUtils() {
    }

    /**
     * 检查group名称是否有效
     *
     * @param group 组名
     * @return 是：有效；否：无效
     */
    public static boolean isValidGroupName(String group) {
        return PATTERN.matcher(group).matches();
    }

    /**
     * 重新构建合法的group名称，Nacos的group只支持英文字符和四种特殊符号('.',':','-','_')。此处'.'替换'/'代表父子节点层级；':'替换'='；'&'替换'_'。
     *
     * @param group 组名
     * @return 合法化的group名称
     */
    public static String reBuildGroup(String group) {
        return group.replace('=', ':').replace('&', '_').replace('/', '.');
    }
}
