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

package com.huawei.flowcontrol.core.datasource.zookeeper;

import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.flowcontrol.core.datasource.DataSourceUpdateSupport;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * zookeeper作为数据源，基于agent core配置中心
 * <h3>|保留用于适配原生的zookeeper路径|</h3>
 *
 * @param <T> 规则
 * @author zhouss
 * @since 2021-11-26
 */
public class ZookeeperCoreDataSource<T> extends AbstractDataSource<DynamicConfigEvent, List<T>>
    implements DataSourceUpdateSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private DynamicConfigEvent event;

    private final String key;

    private final String group;

    public ZookeeperCoreDataSource(final String key, final String group, final Class<T> ruleClass) {
        super(new Converter<DynamicConfigEvent, List<T>>() {
            @Override
            public List<T> convert(DynamicConfigEvent receivedEvent) {
                if (receivedEvent == null || !StringUtils.equal(receivedEvent.getKey(), key)) {
                    return Collections.emptyList();
                }
                if (receivedEvent.getEventType() == DynamicConfigEventType.DELETE) {
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
        this.group = group;
        this.key = key;
        initConfigListener();
    }

    @Override
    public DynamicConfigEvent readSource() {
        return event;
    }

    @Override
    public void close() {
        ServiceManager.getService(DynamicConfigService.class).removeConfigListener(key, group);
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void update(DynamicConfigEvent receivedEvent) {
        this.event = receivedEvent;
        try {
            getProperty().updateValue(loadConfig(receivedEvent));
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Updated rule failed, %s", ex.getMessage()));
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private void initConfigListener() {
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        DynamicConfigListener listener = new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent receivedEvent) {
                update(receivedEvent);
            }
        };
        try {
            service.addConfigListener(key, group, listener, true);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Added listener failed, key : %s, group : %s",
                key, group));
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public void removeListener() {
        try {
            final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
            service.removeConfigListener(key, group);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Removed listener failed, key : %s, group : %s",
                key, group));
        }
    }
}
