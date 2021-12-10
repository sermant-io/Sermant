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

package com.huawei.sermant.core.lubanops.bootstrap.log;

import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import java.io.File;

/**
 * 日志目录工具类
 * @author
 */
public class LogPathUtils {

    private static String appName;

    private static String instanceName;

    /**
     * 获取临时目录
     * @return
     */
    public static String getLogPath() {
        String userHome = System.getProperty("user.home");
        StringBuilder logPath = new StringBuilder();
        logPath.append(userHome).append(File.separator).append("apm").append(File.separator);
        if ((!StringUtils.isBlank(appName)) && (!StringUtils.isBlank(instanceName))) {
            logPath.append("instances")
                .append(File.separator)
                .append(appName)
                .append("-")
                .append(instanceName)
                .append(File.separator);
        }
        return logPath.toString();
    }

    public static void build(String appName, String instanceName) {
        LogPathUtils.appName = appName;
        LogPathUtils.instanceName = instanceName;
    }
}
