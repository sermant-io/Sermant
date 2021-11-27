/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.example.demo.common.DemoLogger;

/**
 * 动态配置示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/26
 */
public class DemoDynaConfService implements PluginService {
    private DynamicConfigurationService service;

    /**
     * 如果是zookeeper实现，修改{@code /javamesh/demo/test}的值以观察动态配置效果
     */
    @Override
    public void start() {
        service = ServiceManager.getService(DynamicConfigurationFactoryService.class).getDynamicConfigurationService();
        service.addConfigListener("/demo/test", "javamesh", new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                DemoLogger.println("[DemoDynaConfService]-" + event.toString());
            }
        });
    }

    @Override
    public void stop() {
        try {
            service.close();
        } catch (Exception ignored) {
        }
    }
}
