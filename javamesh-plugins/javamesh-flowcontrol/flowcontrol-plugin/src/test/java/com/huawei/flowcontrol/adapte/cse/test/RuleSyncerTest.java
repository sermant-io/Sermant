/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.test;

import java.util.Collections;

import org.junit.Test;

import com.huawei.flowcontrol.adapte.cse.RuleSyncer;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;
import com.huawei.javamesh.core.common.CommonConstant;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.ServiceManager;

/**
 * 同步测试
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class RuleSyncerTest {
    @Test
    public void testSync() throws InterruptedException {
        LoggerFactory.init(Collections.singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, new Object()));
        ServiceManager.initServices();
        final RuleSyncer ruleSyncer = new RuleSyncer();
        CseServiceMeta.getInstance().setServiceName("discovery");
        CseServiceMeta.getInstance().setApp("sc");
        CseServiceMeta.getInstance().setCustomLabelValue("default");
        CseServiceMeta.getInstance().setEnvironment("producation");
        CseServiceMeta.getInstance().setCustomLabel("public");
        CseServiceMeta.getInstance().setProject("default");
        ruleSyncer.start();
    }
}
