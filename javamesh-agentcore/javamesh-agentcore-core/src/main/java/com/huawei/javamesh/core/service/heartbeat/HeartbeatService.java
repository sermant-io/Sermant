/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.service.heartbeat;

import com.huawei.javamesh.core.service.BaseService;

/**
 * 心跳服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public interface HeartbeatService extends BaseService {

    /**
     * 心跳的插件名称键
     */
    String PLUGIN_NAME_KEY = "pluginName";

    /**
     * 心跳的插件版本键
     */
    String PLUGIN_VERSION_KEY = "pluginVersion";

    /**
     * 设置额外信息
     *
     * @param extInfoProvider 其他信息的提供者
     */
    void setExtInfo(ExtInfoProvider extInfoProvider);
}
