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

package com.huawei.flowcontrol.core.datasource;

import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 数据源
 *
 * @param <T> 数据源
 * @author zhouss
 * @since 2021-11-26
 */
public class DefaultDataSource<T> extends AbstractDataSource<DynamicConfigEvent, List<T>> implements
    DataSourceUpdateSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 规则键 ruleMap.put("FlowRule", new FlowRuleWrapper()); ruleMap.put("DegradeRule", new DegradeRuleWrapper());
     * ruleMap.put("SystemRule", new SystemRuleWrapper()); ruleMap.put("AuthorityRule", new AuthorityRuleWrapper());
     */
    private final String ruleKey;

    /**
     * 上一次事件
     */
    private DynamicConfigEvent event;

    public DefaultDataSource(final Class<T> ruleClass, String ruleKey) {
        super(new Converter<DynamicConfigEvent, List<T>>() {
            @Override
            public List<T> convert(DynamicConfigEvent receivedEvent) {
                if (receivedEvent == null || receivedEvent.getEventType() == DynamicConfigEventType.DELETE) {
                    return Collections.emptyList();
                }
                try {
                    return JSONArray.parseArray(receivedEvent.getContent(), ruleClass);
                } catch (JSONException ex) {
                    LOGGER.warning(String.format(Locale.ENGLISH, "Formatted rule failed! %s, raw value: [%s]",
                        ex.getMessage(), receivedEvent.getContent()));
                    return Collections.emptyList();
                }
            }
        });
        this.ruleKey = ruleKey;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void update(DynamicConfigEvent receivedEvent) {
        final String key = receivedEvent.getKey();
        if (!StringUtils.equal(ruleKey, key)) {
            return;
        }
        this.event = receivedEvent;
        try {
            getProperty().updateValue(loadConfig(receivedEvent));
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Updated rule failed, %s", ex.getMessage()));
        }
    }

    @Override
    public DynamicConfigEvent readSource() {
        return this.event;
    }

    @Override
    public void close() {
    }
}
