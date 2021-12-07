/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.service.dynamicconfig.kie;

import com.huawei.javamesh.core.common.CommonConstant;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import com.huawei.javamesh.core.service.dynamicconfig.kie.client.http.HttpResult;
import com.huawei.javamesh.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.javamesh.core.service.dynamicconfig.utils.LabelGroupUtils;
import org.junit.Assert;
import org.junit.Before;
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
public class PublishTest {

    @Before
    public void initLog() {
        LoggerFactory.init(Collections.singletonMap(CommonConstant.LOG_SETTING_FILE_KEY, "log"));
    }

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

}
