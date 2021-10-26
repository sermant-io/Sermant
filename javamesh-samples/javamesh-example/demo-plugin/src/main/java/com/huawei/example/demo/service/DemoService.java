/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import com.huawei.apm.bootstrap.boot.PluginService;

/**
 * 示例服务，本示例中将展示如何编写一个插件服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoService implements PluginService {
    @Override
    public void init() {
        System.out.println("[DemoService]-init");
    }

    @Override
    public void stop() {
        System.out.println("[DemoService]-stop");
    }
}
