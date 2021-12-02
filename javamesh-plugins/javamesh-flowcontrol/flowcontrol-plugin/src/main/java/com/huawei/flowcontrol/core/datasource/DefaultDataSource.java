/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.flowcontrol.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 数据源
 *
 * @author zhouss
 * @since 2021-11-26
 */
public class DefaultDataSource<T> extends AbstractDataSource<ConfigChangedEvent, List<T>> implements DataSourceUpdateSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 规则键
     * ruleMap.put("FlowRule", new FlowRuleWrapper());
     * ruleMap.put("DegradeRule", new DegradeRuleWrapper());
     * ruleMap.put("SystemRule", new SystemRuleWrapper());
     * ruleMap.put("AuthorityRule", new AuthorityRuleWrapper());
     */
    private final String ruleKey;

    /**
     * 上一次事件
     */
    private ConfigChangedEvent event;

    public DefaultDataSource(final Class<T> ruleClass, String ruleKey) {
        super(new Converter<ConfigChangedEvent, List<T>>() {
            @Override
            public List<T> convert(ConfigChangedEvent event) {
                if (event == null || event.getChangeType() == ConfigChangeType.DELETED) {
                    return Collections.emptyList();
                }
                try {
                    return JSONArray.parseArray(event.getContent(), ruleClass);
                } catch (JSONException ex) {
                    LOGGER.warning(String.format(Locale.ENGLISH, "Formatted rule failed! %s, raw value: [%s]",
                            ex.getMessage(), event.getContent()));
                    return Collections.emptyList();
                }
            }
        });
        this.ruleKey = ruleKey;
    }

    @Override
    public void update(ConfigChangedEvent event) {
        final String key = event.getKey();
        if (!StringUtils.equal(ruleKey, key)) {
            return;
        }
        this.event = event;
        try {
            getProperty().updateValue(loadConfig(event));
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Updated rule failed, %s", ex.getMessage()));
        }

    }

    @Override
    public ConfigChangedEvent readSource() {
        return this.event;
    }

    @Override
    public void close() {

    }
}
