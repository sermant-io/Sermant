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

package com.huawei.flowrecord.utils;

public class DubboUtil {
    public static String buildServiceKey(String path, String group, String version) {
        StringBuilder buf = new StringBuilder();
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(path);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public static String buildSourceKey(String serviceUniqueName, String methodName) {
        StringBuilder buf = new StringBuilder();
        if (serviceUniqueName != null && serviceUniqueName.length() > 0 && methodName != null && methodName.length() > 0) {
            return buf.append(serviceUniqueName).append("/").append(methodName).toString();
        } else {
            throw new IllegalArgumentException(
                    String.format("build sourceKey failed, serviceUniqueName:[%s], methosName:[%s]", serviceUniqueName, methodName)
            );
        }
    }
}
