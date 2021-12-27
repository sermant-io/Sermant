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

package com.huawei.route.common.gray.listener;

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.gray.label.entity.VersionFrom;
import com.huawei.route.common.utils.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * 配置监听器
 *
 * @author pengyuyi
 * @date 2021/11/29
 */
public class GrayDynamicConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LogFactory.getLogger();

    private final String labelName;

    private final String key;

    /**
     * 构造方法
     *
     * @param labelName 缓存的标签名
     * @param key 配置的key
     */
    public GrayDynamicConfigListener(String labelName, String key) {
        super();
        this.labelName = labelName;
        this.key = key;
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (event.getKey() == null || !event.getKey().equals(key)) {
            return;
        }
        GrayConfiguration grayConfiguration = LabelCache.getLabel(labelName);
        Map<String, List<Rule>> routeRule = new LinkedHashMap<String, List<Rule>>();
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            resetGrayConfiguration(grayConfiguration);
            return;
        }
        Yaml yaml = new Yaml();
        Map<String, Map<String, Map<String, String>>> load = yaml.load(event.getContent());
        if (isInValidMap(load, grayConfiguration)) {
            resetGrayConfiguration(grayConfiguration);
            return;
        }
        Map<String, Map<String, String>> servicecomb = load.get(GrayConstant.GRAY_CONFIG_SERVICECOMB_KEY);
        if (isInValidMap(servicecomb, grayConfiguration)) {
            return;
        }
        Map<String, String> routeRuleMap = servicecomb.get(GrayConstant.GRAY_CONFIG_ROUTE_RULE_KEY);
        if (isInValidMap(routeRuleMap, grayConfiguration)) {
            return;
        }
        Yaml routeRuleYaml = new Yaml();
        for (Entry<String, String> entry : routeRuleMap.entrySet()) {
            List<Map<String, String>> routeRuleList = routeRuleYaml.load(entry.getValue());
            if (CollectionUtils.isEmpty(routeRuleList)) {
                continue;
            }
            Map<String, String> versionFromMap = routeRuleList.get(0);
            String versionFrom = versionFromMap.get(GrayConstant.GRAY_CONFIG_VERSION_FROM_KEY);
            if (StringUtils.isBlank(versionFrom)) {
                grayConfiguration.setVersionFrom(VersionFrom.REGISTER_MSG);
            } else {
                grayConfiguration.setVersionFrom(VersionFrom.valueOf(versionFrom.toUpperCase(Locale.ROOT)));
                routeRuleList.remove(0);
            }
            List<Rule> list = JSONArray.parseArray(JSONObject.toJSONString(routeRuleList), Rule.class);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            routeRule.put(entry.getKey(), list);
        }
        grayConfiguration.setRouteRule(routeRule);
        grayConfiguration.setOn(!CollectionUtils.isEmpty(routeRule));
        grayConfiguration.setValid(!CollectionUtils.isEmpty(routeRule));
        LOGGER.info(String.format("Config [%s] has been %s ", event.getKey(), event.getEventType()));
    }

    private boolean isInValidMap(Map<?, ?> map, GrayConfiguration grayConfiguration) {
        if (CollectionUtils.isEmpty(map)) {
            resetGrayConfiguration(grayConfiguration);
            return true;
        }
        return false;
    }

    private void resetGrayConfiguration(GrayConfiguration grayConfiguration) {
        grayConfiguration.setRouteRule(Collections.<String, List<Rule>>emptyMap());
        grayConfiguration.setOn(false);
        grayConfiguration.setValid(false);
    }
}