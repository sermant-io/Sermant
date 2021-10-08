/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.config;

import org.junit.Assert;
import org.junit.Test;

public class PluginConfigTest {
    @Test
    public void configNotNull(){
        Assert.assertNotNull(PluginConfig.httpSuccessStatus);
        Assert.assertNotNull(PluginConfig.mockServerUrl);
        Assert.assertNotNull(PluginConfig.httpTimeout);
        Assert.assertNotNull(PluginConfig.isMock);
    }
}
