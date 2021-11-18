/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.plugin.service.PluginService;

/**
 * 标签初始化
 *
 * @author zhouss
 * @since 2021-11-02
 */
public class LabelInitServiceImpl implements PluginService {
    @Override
    public void start() {
        final LabelConfig config = PluginConfigManager.getPluginConfig(LabelConfig.class);
        if (config.isLabelOpen()) {
            LabelValidService.INSTANCE.start(config.getPort());
        }
    }

    @Override
    public void stop() {
        LabelValidService.INSTANCE.stop();
    }
}
