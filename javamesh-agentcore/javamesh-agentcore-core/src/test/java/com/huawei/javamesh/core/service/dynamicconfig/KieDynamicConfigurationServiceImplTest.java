/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.service.dynamicconfig;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.javamesh.core.common.CommonConstant;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.javamesh.core.service.dynamicconfig.utils.LabelGroupUtils;

/**
 * kie配置中心测试
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigurationServiceImplTest {

    @Before
    public void initLog() {
        LoggerFactory.init(Collections.singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, "log"));
    }

    @Test
    public void testListener() {
        // 初始化日志
        final SubscriberManager subscriberManager = new SubscriberManager("http://127.0.0.1:30110");
        final String group = LabelGroupUtils.createLabelGroup(Collections.singletonMap("version", "1.0"));
        final ConfigurationListener configurationListener = new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                System.out.println(event.getContent());
            }
        };
        subscriberManager.addGroupListener(group, configurationListener);
        Assert.assertTrue(subscriberManager.removeGroupListener(group, configurationListener));
    }
}
