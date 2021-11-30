/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.listener;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
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
public class GrayConfigListener implements ConfigurationListener {
    private static final Logger LOGGER = LogFactory.getLogger();

    private final String labelName;

    public GrayConfigListener(String labelName) {
        super();
        this.labelName = labelName;
    }

    @Override
    public void process(ConfigChangedEvent event) {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(labelName);
        Map<String, List<Rule>> routeRule = new LinkedHashMap<String, List<Rule>>();
        if (event.getChangeType() == ConfigChangeType.DELETED) {
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
        LOGGER.info(String.format("Config [%s] has been %s ", event.getKey(), event.getChangeType()));
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
