/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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
