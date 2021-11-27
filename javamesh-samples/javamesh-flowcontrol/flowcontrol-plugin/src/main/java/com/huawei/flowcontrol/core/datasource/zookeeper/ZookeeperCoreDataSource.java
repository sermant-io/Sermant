/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.flowcontrol.core.datasource.DataSourceUpdateSupport;
import com.huawei.flowcontrol.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * zookeeper作为数据源，基于agent core配置中心
 *
 * @author zhouss
 * @since 2021-11-26
 */
public class ZookeeperCoreDataSource<T> extends AbstractDataSource<ConfigChangedEvent, List<T>>
        implements DataSourceUpdateSupport {
    private static final Logger LOGGER = LogFactory.getLogger();

    private ConfigChangedEvent event;

    private final String key;

    private final String group;

    private ConfigurationListener listener;

    public ZookeeperCoreDataSource(final String key, final String group, final Class<T> ruleClass) {
        super(new Converter<ConfigChangedEvent, List<T>>() {
            @Override
            public List<T> convert(ConfigChangedEvent event) {
                if (event == null || !StringUtils.equal(event.getKey(), key)) {
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
        this.group = group;
        this.key = key;
        initConfigListener();
    }

    @Override
    public ConfigChangedEvent readSource() {
        return event;
    }

    @Override
    public void close() {

    }

    @Override
    public void update(ConfigChangedEvent event) {
        this.event = event;
        try {
            getProperty().updateValue(loadConfig(event));
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Updated rule failed, %s", ex.getMessage()));
        }
    }

    private void initConfigListener() {
        final DynamicConfigurationFactoryService service = ServiceManager.getService(DynamicConfigurationFactoryService.class);
        listener = new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                update(event);
            }
        };
        try {
            service.getDynamicConfigurationService().addConfigListener(key, group, listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Added listener failed, key : %s, group : %s",
                    key, group));
        }
    }

    public void removeListener() {
        try {
            final DynamicConfigurationFactoryService service = ServiceManager.getService(DynamicConfigurationFactoryService.class);
            service.getDynamicConfigurationService().removeConfigListener(key, group, listener);
        } catch (Exception ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Removed listener failed, key : %s, group : %s",
                    key, group));
        }
    }
}
