/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.monitor.common.service;

import com.huawei.apm.core.plugin.service.PluginService;

/**
 * Database peer 解析服务
 */
public interface DatabasePeerParseService extends PluginService {

    /**
     * 把URL解析成database peer
     *
     * @param url database url
     * @return database peer
     */
    String parse(String url);
}
