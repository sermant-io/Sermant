/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.kie;

import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.util.PluginConfigUtil;
import com.huawei.flowcontrol.core.datasource.kie.util.KieConfigClient;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigItem;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigLabels;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigResponse;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 数据源kie
 *
 * @param <T> T
 * @author hanpeng
 * @since 2020-11-12
 */
public class KieAutoRefreshDataSource<T> extends AutoRefreshDataSource<String, T> {
    private static final long DEFAULT_REFRESH_MS = 3000L;

    private final String ruleKey;

    private String lastRules;

    public KieAutoRefreshDataSource(Converter<String, T> configParser, String ruleName, String defaultRule) {
        this(configParser, DEFAULT_REFRESH_MS, ruleName, defaultRule);
    }

    public KieAutoRefreshDataSource(Converter<String, T> configParser, long recommendRefreshMs,
        String ruleKey, String defaultRule) {
        super(configParser, recommendRefreshMs);

        this.ruleKey = ruleKey;
        this.lastRules = defaultRule;

        firstLoad();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void firstLoad() {
        RecordLog.info(String.format(Locale.ENGLISH, "First load config, ruleKey: %s.", this.ruleKey));

        try {
            getProperty().updateValue(loadConfig(this.lastRules));
        } catch (Exception e) {
            RecordLog.error("First loadConfig exception", e);
        }
    }

    @Override
    public String readSource() {
        String kieServerAddress = PluginConfigUtil.getValueByKey(ConfigConst.CONFIG_KIE_ADDRESS).trim();
        if ("".equals(kieServerAddress)) {
            return lastRules;
        }

        KieConfigResponse config = KieConfigClient.getConfig(kieServerAddress + ConfigConst.KIE_RULES_URI);
        if (config != null) {
            List<KieConfigItem> data = config.getData();
            if (data == null || data.isEmpty()) {
                return lastRules;
            }
            final List<JSONObject> results = new ArrayList<JSONObject>();
            for (KieConfigItem item : data) {
                if (item == null || !isTargetItem(item)) {
                    continue;
                }
                results.add(JSON.parseObject(item.getValue()));
            }
            this.lastRules = JSON.toJSONString(results);
        }
        return lastRules;
    }

    /**
     * Judge target item by conditions.
     *
     * @param item config item
     * @return judge result
     */
    private boolean isTargetItem(KieConfigItem item) {
        KieConfigLabels labels = item.getLabels();
        if (labels == null) {
            return false;
        }
        return this.ruleKey.equals(item.getKey())
            && AppNameUtil.getAppName().equals(labels.getService());
    }
}
