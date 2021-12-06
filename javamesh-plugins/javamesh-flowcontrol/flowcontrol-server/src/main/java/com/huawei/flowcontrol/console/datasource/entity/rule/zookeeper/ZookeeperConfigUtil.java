/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol.console.datasource.entity.rule.zookeeper;

import com.huawei.flowcontrol.console.util.DataType;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;

/**
 * zookeeper工具类
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component
public class ZookeeperConfigUtil {
    private ZookeeperConfigUtil() {
    }

    /**
     * 获取zookeeper存储流控路径
     *
     * @param appName 应用名
     * @return 返回路径
     */
    public static String getPath(String appName) {
        StringBuilder stringBuilder = new StringBuilder(DataType.RULE_ROOT_PATH.getDataType());

        // 判断appname是否为空，为空直接返回
        if (StringUtils.isBlank(appName)) {
            return stringBuilder.toString();
        }

        // 判断appname是否以/开始
        if (appName.startsWith("/")) {
            stringBuilder.append(appName);
        } else {
            stringBuilder.append("/")
                .append(appName);
        }
        String result = stringBuilder.toString().replace("/+", Matcher.quoteReplacement(File.separator));
        return result.replace("\\\\+", Matcher.quoteReplacement(File.separator));
    }

    /**
     * 获取告警规则的路径
     *
     * @return 告警规则的路径
     */
    public static String getRulesPath() {
        StringBuilder rulesPath = new StringBuilder(DataType.YAML_ROOT_PATH.getDataType());
        rulesPath.append(DataType.YAML.getDataType());
        return rulesPath.toString();
    }
}
