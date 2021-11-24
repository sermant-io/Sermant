/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig;

import com.huawei.apm.core.service.dynamicconfig.kie.KieDynamicConfigurationServiceImpl;
import com.huawei.apm.core.service.dynamicconfig.kie.client.kie.KieRequestFactory;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import org.junit.Before;
import org.junit.Test;

/**
 * kie配置中心测试
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigurationServiceImplTest {
    private KieDynamicConfigurationServiceImpl instance;

    @Before
    public void setUp() {
        instance = KieDynamicConfigurationServiceImpl.getInstance();
    }

    @Test
    public void testAddListener() throws InterruptedException {
        instance.addListener(KieRequestFactory.buildKieRequest("20", new String[]{KieRequestFactory.buildLabel("version", "1.0")}),
                new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                System.out.println(event.getContent());
            }
        });
    }
}
