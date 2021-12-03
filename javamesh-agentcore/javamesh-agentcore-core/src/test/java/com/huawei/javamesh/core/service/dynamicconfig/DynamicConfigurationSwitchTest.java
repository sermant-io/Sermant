/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.service.dynamicconfig;

import java.util.Collections;
import java.util.ServiceLoader;

import org.junit.Before;
import org.junit.Test;

import com.huawei.javamesh.core.common.CommonConstant;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.BaseService;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigurationService;

/**
 * kie与zookeeper切换测试
 *
 * @author zhouss
 * @since 2021-11-29
 */
public class DynamicConfigurationSwitchTest {
    private DynamicConfigurationFactoryService service;

    @Before
    public void init() {
        LoggerFactory.init(Collections.singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, "log"));
    }

    @Test
    public void test() throws InterruptedException {
        // zk
        loadDynamicConfiguration(DynamicConfigType.ZOO_KEEPER, null);
        final DynamicConfigurationService zkService = service.getDynamicConfigurationService();
        addListener(zkService);

        // kie
        loadDynamicConfiguration(DynamicConfigType.KIE, null);
        final DynamicConfigurationService kieService = service.getDynamicConfigurationService();
        addListener(kieService);

        // Thread.sleep(1000000);

    }

    private void loadDynamicConfiguration(DynamicConfigType type, String url) {
        ServiceLoader<BaseService> sl = ServiceLoader.load(BaseService.class);
        Config.getInstance().dynamic_config_type = type;
        if (url != null) {
            Config.getInstance().kie_url = url;
        }
        for (BaseService cs : sl) {
            if (cs.getClass().toString().contains("DynamicConfigurationFactoryServiceImpl")) {
                service = (DynamicConfigurationFactoryService) cs;
                break;
            }
        }
    }

    private void addListener(DynamicConfigurationService dynamicConfigurationService) {
        final ConfigurationListener configurationListener = new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                System.out.println(event);
            }
        };
        dynamicConfigurationService.addConfigListener("/zk", "test", configurationListener);
    }

}
