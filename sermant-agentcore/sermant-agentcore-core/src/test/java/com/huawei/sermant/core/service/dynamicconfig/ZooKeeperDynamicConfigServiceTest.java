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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigServiceType;
import com.huawei.sermant.core.service.dynamicconfig.config.DynamicConfig;

public class ZooKeeperDynamicConfigServiceTest {
    private Logger logger;

    private DynamicConfigService service;

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        final URL logbackSettingURL = getClass().getResource("/logback-test.xml");
        Assert.assertNotNull(logbackSettingURL);
        LoggerFactory.init(Collections.singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, logbackSettingURL.getPath()));
        final DynamicConfig dynamicConfig = new DynamicConfig();
        final Field serverAddressField = DynamicConfig.class.getDeclaredField("serverAddress");
        serverAddressField.setAccessible(true);
        serverAddressField.set(dynamicConfig, "127.0.0.1:2181");
        final Field serviceTypeField = DynamicConfig.class.getDeclaredField("serviceType");
        serviceTypeField.setAccessible(true);
        serviceTypeField.set(dynamicConfig, DynamicConfigServiceType.ZOOKEEPER);
        final Field configMapField = ConfigManager.class.getDeclaredField("CONFIG_MAP");
        configMapField.setAccessible(true);
        final Map configs = (Map) configMapField.get(null);
        configs.put("dynamic.config", dynamicConfig);
        service = new BufferedDynamicConfigService();
        service.start();
        Thread.sleep(1000);
        logger = LoggerFactory.getLogger();
    }

    @Test
    public void listenerTest() throws InterruptedException {
        service.addConfigListener("dummy-key", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                logger.warning("Without group event: " + event);
            }
        });
        service.addConfigListener("dummy-key", "dummy-group", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                logger.warning("With group event: " + event);
            }
        });
        service.addGroupListener("dummy-group", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                logger.warning("Only group event: " + event);
            }
        });

        dataTest();

        service.removeConfigListener("dummy-key");
        service.removeConfigListener("dummy-key", "dummy-group");
        service.removeGroupListener("dummy-group");

        dataTest();
    }

    private void dataTest() throws InterruptedException {
        logger.warning("----------------------------------------------------------add");
        service.publishConfig("dummy-key", "value1");
        service.publishConfig("dummy-key", "dummy-group", "value2");
        service.publishConfig("dummy-path/dummy-key", "dummy-group", "value3");

        Thread.sleep(1000);

        logger.warning("----------------------------------------------------------modify");
        service.publishConfig("dummy-key", "value4");
        service.publishConfig("dummy-key", "dummy-group", "value5");
        service.publishConfig("dummy-path/dummy-key", "dummy-group", "value6");

        Thread.sleep(1000);

        logger.warning("----------------------------------------------------------check");
        Assert.assertEquals("value4", service.getConfig("dummy-key"));
        Assert.assertEquals("value5", service.getConfig("dummy-key", "dummy-group"));
        Assert.assertEquals("value6", service.getConfig("dummy-path/dummy-key", "dummy-group"));
        logger.warning("list keys: " + service.listKeys());
        logger.warning("list keys from group: " + service.listKeysFromGroup("dummy-group"));

        logger.warning("----------------------------------------------------------remove");
        service.removeConfig("dummy-key");
        service.removeConfig("dummy-key", "dummy-group");
        service.removeConfig("dummy-path/dummy-key", "dummy-group");

        Thread.sleep(1000);
    }
}
