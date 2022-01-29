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

package com.huawei.sermant.core.agent.common;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Listener;
import com.huawei.sermant.core.lubanops.bootstrap.commons.ConditionOnVersion;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 插件版本检查工具
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public class VersionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private String version;

    private Listener listener;

    public VersionChecker(String version, Listener listener) {
        this.version = version;
        this.listener = listener;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public boolean check() {
        ConditionOnVersion conditionOnVersion = listener.getClass().getAnnotation(ConditionOnVersion.class);
        if (null == conditionOnVersion) {
            return true;
        }
        for (String definedVersion : conditionOnVersion.versions()) {
            if (matchVersion(definedVersion, version)) {
                return true;
            }
        }
        LOGGER.log(Level.INFO,
                String.format("[APM TRANSFORMER]transformer not trigger because version[%s] match miss.", version));
        return false;
    }

    @SuppressWarnings({"checkstyle:RegexpSinglelineJava", "checkstyle:IllegalCatch"})
    public boolean matchVersion(String definedVersion, String packageName) {

        try {
            String[] regVersionArr = definedVersion.split("\\.");
            if (!StringUtils.isDigit(regVersionArr[0])) {
                LOGGER.log(Level.SEVERE,
                        String.format("[APM TRANSFORMER]bad condition on version defined[%s].", definedVersion));
                return true;
            }
            int offset = packageName.indexOf(regVersionArr[0] + ".");
            if (offset < 0) {
                return false;
            }
            for (int i = 0; i < regVersionArr.length; i++) {
                int startIndex = offset;
                int endIndex = packageName.indexOf(".", offset);
                if (endIndex <= startIndex) {
                    return false;
                }
                String matchSegStr = packageName.substring(startIndex, endIndex);
                if (i == regVersionArr.length - 1 && !StringUtils.isDigit(matchSegStr)) {
                    endIndex = packageName.indexOf("-", offset);
                    matchSegStr = packageName.substring(startIndex, endIndex);
                }
                offset = endIndex + 1;
                if (regVersionArr[i].equalsIgnoreCase("x")) {
                    continue;
                }
                if (!regVersionArr[i].equals(matchSegStr)) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    String.format("[APM TRANSFORMER]matchVersion error occur [definedVersion:%s][packageName:%s].",
                            definedVersion, packageName),
                    e);
        }
        return true;
    }

}
