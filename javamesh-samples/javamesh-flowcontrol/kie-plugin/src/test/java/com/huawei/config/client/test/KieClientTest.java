/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.client.test;

import com.huawei.config.client.ClientUrlManager;
import com.huawei.config.kie.KieClient;
import com.huawei.config.kie.KieRequest;
import com.huawei.config.kie.KieRequestFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * KieClient测试
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieClientTest {

    private boolean kieUrlEnabled = true;

    private final String url = "http://172.31.100.55:30110";

    @Before
    public void testConnect() {
        // 测试kie地址连接，如果连接失败，跳过查询的测试
        final ClientUrlManager clientUrlManager = new ClientUrlManager(url);
        final KieClient kieClient = new KieClient(clientUrlManager);
        try {
            kieClient.queryConfigurations(new KieRequest());
        } catch (Exception e) {
            kieUrlEnabled = false;
        }
    }

    @Test
    public void kieClientTest() {
        if (!kieUrlEnabled) {
            return;
        }
        final ClientUrlManager clientUrlManager = new ClientUrlManager(url);
        final KieClient kieClient = new KieClient(clientUrlManager);
        kieClient.queryConfigurations(new KieRequest());
    }
    @Test
    public void kieClientLabelTest() {
        if (!kieUrlEnabled) {
            return;
        }
        final ClientUrlManager clientUrlManager = new ClientUrlManager(url);
        final KieClient kieClient = new KieClient(clientUrlManager);
        kieClient.queryConfigurations(
                KieRequestFactory.buildKieRequest(new String[]{"label=version:1.0", "label=app:region-A", "label=serviceName:helloService"}));
    }

    @Test
    public void kieClientLabelMapTest() {
        if (!kieUrlEnabled) {
            return;
        }
        final ClientUrlManager clientUrlManager = new ClientUrlManager(url);
        final KieClient kieClient = new KieClient(clientUrlManager);
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("version", "1.0");
        map.put("app", "region-A");
        map.put("serviceName", "helloService");
        kieClient.queryConfigurations(
                KieRequestFactory.buildKieRequest("1", null, map));
    }
}
