/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import java.util.logging.Logger;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.plugin.service.PluginServiceManager;
import com.huawei.example.demo.common.DemoLogger;

/**
 * 示例服务，本示例中将展示如何编写一个插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoSimpleService implements PluginService {
    @Override
    public void start() {
        DemoLogger.println("[DemoSimpleService]-start");
    }

    @Override
    public void stop() {
        DemoLogger.println("[DemoSimpleService]-stop");
    }

    /**
     * 主动调用的方法，将调用{@link DemoComplexService#passiveFunc()}方法
     */
    public void activeFunc() {
        DemoLogger.println("[DemoSimpleService]-activeFunc");
        final DemoComplexService service = PluginServiceManager.getPluginService(DemoComplexService.class);
        service.passiveFunc();
    }

    /**
     * 被动调用的方法，将被{@link DemoComplexService#activeFunc()}方法调用
     */
    public void passiveFunc() {
        DemoLogger.println("[DemoSimpleService]-passiveFunc");
    }
}
