/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.service.dynamicconfig;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.sermant.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

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
