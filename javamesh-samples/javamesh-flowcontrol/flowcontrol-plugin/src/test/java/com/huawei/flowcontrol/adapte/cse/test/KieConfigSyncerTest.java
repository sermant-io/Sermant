/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.test;

import com.huawei.flowcontrol.adapte.cse.KieConfigSyncer;
import org.junit.Test;

/**
 * 同步测试
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieConfigSyncerTest {
    @Test
    public void testSync() throws InterruptedException {
        final KieConfigSyncer kieConfigSyncer = new KieConfigSyncer();
        kieConfigSyncer.init();
        Thread.sleep(100000000);
    }
}
