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

import java.net.URL;
import java.util.Collections;
import java.util.Locale;

import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.config.DynamicConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

/**
 * kie配置中心测试
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigServiceTest extends BaseTest {

    @Test
    public void testListener() {
        // 初始化日志
        final SubscriberManager subscriberManager = new SubscriberManager("http://127.0.0.1:30110");
        final String group = LabelGroupUtils.createLabelGroup(Collections.singletonMap("version", "1.0"));
        final DynamicConfigListener dynamicConfigListener = new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                System.out.println(event.getContent());
            }
        };
        subscriberManager.addGroupListener(group, dynamicConfigListener, true);
        Assert.assertTrue(subscriberManager.removeGroupListener(group, dynamicConfigListener));
    }

    @Test
    public void testAddListener() {
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.addConfigListener("rule", "groupTest=aa", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                System.out.printf(Locale.ENGLISH, "key notify %s key %s, content: %s%n"
                , event.getEventType(), event.getKey(), event.getContent());
            }
        });
        service.addGroupListener("service=flowControlDemo", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                System.out.printf(Locale.ENGLISH, "group notify %s key %s, content: %s%n"
                        , event.getEventType(), event.getKey(), event.getContent());
            }
        });
    }

    @Test
    public void testRemoveConfig() {
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.removeConfig("rule3", "e=f&a=b&c=d");
    }
}
