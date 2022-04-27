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

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.BaseTest;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.http.HttpResult;
import com.huaweicloud.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

import org.junit.Assert;
import org.junit.Test;

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
    public void testPublish() {
        final SubscriberManager subscriberManager = new SubscriberManager("http://127.0.0.1:30110");
        final boolean result = subscriberManager.publishConfig(UUID.randomUUID().toString(),
                LabelGroupUtils.createLabelGroup(Collections.singletonMap("service", "testing")),
                "It just for test");
        Assert.assertTrue(result);
    }

    @Test
    public void testPublishSameKey() {
        String key = "rule3";
        String group = "a=b&c=d&e=f";
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.publishConfig(key, group, "1");
        service.publishConfig(key, group, "3");
        final String config = service.getConfig(key, group);
        Assert.assertEquals("3", config);
    }
}
