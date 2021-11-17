/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.core.config.common.ConfigTypeKey;
import com.huawei.apm.core.plugin.config.PluginConfig;

/**
 * 标签库配置
 *
 * @author zhouss
 * @since 2021-11-02
 */
@ConfigTypeKey("route.label.plugin")
public class LabelConfig implements PluginConfig {
    /**
     * 标签库端口
     * 默认8001
     */
    private int port = 8001;

    /**
     * 标签库开关
     * 默认开启
     */
    private boolean labelOpen = true;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isLabelOpen() {
        return labelOpen;
    }

    public void setLabelOpen(boolean labelOpen) {
        this.labelOpen = labelOpen;
    }
}
