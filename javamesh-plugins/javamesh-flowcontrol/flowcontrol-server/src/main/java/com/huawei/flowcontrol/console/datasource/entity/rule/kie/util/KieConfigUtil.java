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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.util;

import com.alibaba.fastjson.JSON;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigLabel;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * kie服务相关util
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
public class KieConfigUtil {
    /**
     * Judge target item by conditions.
     *
     * @param key  配置的key
     * @param app  应用名称
     * @param item config item
     * @return judge result
     */
    public static boolean isTargetItem(String key, String app, KieConfigItem item) {
        if (Objects.isNull(item) || Objects.isNull(item.getLabels()) || StringUtils.isEmpty(key)
            || Objects.isNull(app)) {
            return false;
        }

        KieConfigLabel configLabel = item.getLabels();

        return key.equals(item.getKey())
            && app.equals(configLabel.getService());
    }

    public static <T> T parseKieConfig(Class<T> ruleClass, KieConfigItem item) {
        return JSON.parseObject(item.getValue(), ruleClass);
    }
}
