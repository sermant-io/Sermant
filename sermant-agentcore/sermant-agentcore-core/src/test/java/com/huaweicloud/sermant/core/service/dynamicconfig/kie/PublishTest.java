/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.service.dynamicconfig.kie;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.BaseTest;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.http.HttpResult;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.kie.KieClient;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.config.KieDynamicConfig;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * kie客户端测试
 *
 * @author zhouss
 * @since 2021-12-02
 */
public class PublishTest extends BaseTest {

    @Test
    public void testPost() {
        final DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        final Map<String, Object> params = new HashMap<>();
        final HashMap<String, String> map = new HashMap<>();
        map.put("service", "service");
        params.put("key", UUID.randomUUID());
        params.put("value", "1234");
        params.put("labels", map);
        params.put("status", "enabled");
        final HttpResult httpResult = defaultHttpClient.doPost("http://127.0.0.1:30110/v1/default/kie/kv?", params);
        Assert.assertNotNull(httpResult);
    }

    @Test
    public void testPublish() throws NoSuchFieldException, IllegalAccessException {
        MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(KieDynamicConfig.class)).thenReturn(new KieDynamicConfig());
        final SubscriberManager subscriberManager = new SubscriberManager("http://127.0.0.1:30110");
        KieClient kieClient = Mockito.mock(KieClient.class);
        Field kieClientField = SubscriberManager.class.getDeclaredField("kieClient");
        kieClientField.setAccessible(true);
        removeFinalModify(kieClientField);
        kieClientField.set(subscriberManager, kieClient);
        Mockito.when(kieClient.publishConfig(Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(true);
        final boolean result = subscriberManager.publishConfig(UUID.randomUUID().toString(),
                LabelGroupUtils.createLabelGroup(Collections.singletonMap("service", "testing")),
                "It just for test");
        Assert.assertTrue(result);
        configManagerMockedStatic.close();
    }
}
