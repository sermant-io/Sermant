/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.plugin.service.PluginServiceManager;

/**
 * 示例服务，本示例中将展示如何编写一个插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoSimpleService implements PluginService {
    private final Logger logger = LogFactory.getLogger();

    @Override
    public void start() {
        System.out.println("[DemoService]-start");
    }

    @Override
    public void stop() {
        System.out.println("[DemoService]-stop");
    }

    /**
     * 主动调用的方法，将调用{@link DemoComplexService#passiveFunc()}方法
     */
    public void activeFunc() {
        System.out.println("[DemoService]-activeFunc");
        final DemoComplexService service = PluginServiceManager.getPluginService(DemoComplexService.class);
        service.passiveFunc();
    }

    /**
     * 被动调用的方法，将被{@link DemoComplexService#activeFunc()}方法调用
     */
    public void passiveFunc() {
        System.out.println("[DemoService]-passiveFunc");
        logger.info("[DemoService]-passiveFunc");
    }
}
