/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import com.huawei.javamesh.core.plugin.service.PluginService;

/**
 * 复杂服务的接口示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
 */
public interface DemoComplexService extends PluginService {
    /**
     * 主动调用的方法，将调用{@link DemoSimpleService#passiveFunc()}方法
     */
    void activeFunc();

    /**
     * 被动调用的方法，将被{@link DemoSimpleService#activeFunc()}方法调用
     */
    void passiveFunc();
}
