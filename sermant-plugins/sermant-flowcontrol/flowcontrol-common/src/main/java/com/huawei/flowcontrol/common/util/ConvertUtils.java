/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.util;

/**
 * 转换工具类
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class ConvertUtils {
    /**
     * 从dubbo attachment获取version版本
     */
    public static final String DUBBO_ATTACHMENT_VERSION = "version";

    /**
     * dubbo默认缺省值版本号
     */
    public static final String ABSENT_VERSION = "0.0.0";

    private ConvertUtils() {
    }

    /**
     * 构建API路径
     *
     * @param interfaceName 接口名
     * @param version 版本
     * @param methodName 方法名
     * @return api路径
     */
    public static String buildApiPath(String interfaceName, String version, String methodName) {
        if (version == null || "".equals(version) || ABSENT_VERSION.equals(version)) {
            return interfaceName + "." + methodName;
        }

        // com.huawei.dubbotest.service.CTest:version.methodName
        return interfaceName + ":" + version + "." + methodName;
    }
}
