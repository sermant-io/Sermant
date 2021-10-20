/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.datasource.kie.util.KieConfigClient;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigItem;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigLabels;
import com.huawei.flowcontrol.core.datasource.kie.util.response.KieConfigResponse;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据源kie
 *
 * @author hanpeng
 * @param <T> T
 * @since 2020-11-12
 */
public class KieDataSource<T> extends AutoRefreshDataSource<String, T> {
    private static final long DEFAULT_REFRESH_MS = 3000L;

    private final String ruleKey;

    private String lastRules;

    public KieDataSource(Converter<String, T> configParser, String ruleName, String defaultRule) {
        this(configParser, DEFAULT_REFRESH_MS, ruleName, defaultRule);
    }

    public KieDataSource(Converter<String, T> configParser, long recommendRefreshMs,
        String ruleKey, String defaultRule) {
        super(configParser, recommendRefreshMs);

        this.ruleKey = ruleKey;
        this.lastRules = defaultRule;

        firstLoad();
    }

    private void firstLoad() {
        RecordLog.info(String.format("First load config, ruleKey: %s.", this.ruleKey));

        try {
            getProperty().updateValue(loadConfig(this.lastRules));
        } catch (Exception e) {
            RecordLog.error("First loadConfig exception", e);
        }
    }

    @Override
    public String readSource() {
        String kieServerAddress = PluginConfigUtil.getValueByKey(ConfigConst.CONFIG_KIE_ADDRESS).trim();
        // huawei update by zhanghu 20210125
        if ("".equals(kieServerAddress)) {
            return lastRules;
        }

        KieConfigResponse config = KieConfigClient.getConfig(kieServerAddress + ConfigConst.KIE_RULES_URI);
        if (config != null) {
            List<KieConfigItem> data = config.getData();
            // huawei update by zhanghu 20210125
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
