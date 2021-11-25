/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.plugin.service.PluginServiceManager;
import com.huawei.example.demo.config.DemoConfig;
import com.huawei.example.demo.config.DemoServiceConfig;

/**
 * 复杂服务示例实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
 */
public class DemoComplexServiceImpl implements DemoComplexService {
    /**
     * 日志
     */
    private final Logger logger = LogFactory.getLogger();

    @Override
    public void start() {
        System.out.println("[DemoComplexService]-start");
    }

    @Override
    public void stop() {
        System.out.println("[DemoComplexService]-stop");
    }

    @Override
    public void activeFunc() {
        System.out.println("[DemoComplexService]-activeFunc");
        final DemoSimpleService service = PluginServiceManager.getPluginService(DemoSimpleService.class);
        service.passiveFunc();
    }

    @Override
    public void passiveFunc() {
        System.out.println("[DemoComplexService]-passiveFunc");
        final DemoServiceConfig serviceConfig = PluginConfigManager.getPluginConfig(DemoServiceConfig.class);
        System.out.println(getClass().getSimpleName() + ": " + serviceConfig);
        final DemoConfig demoConfig = PluginConfigManager.getPluginConfig(DemoConfig.class);
        System.out.println(getClass().getSimpleName() + ": " + demoConfig);
        logger.info("[DemoService]-passiveFunc");
    }
}
