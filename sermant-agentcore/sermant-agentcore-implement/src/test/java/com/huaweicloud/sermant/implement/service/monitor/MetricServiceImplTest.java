/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.monitor;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.monitor.config.MonitorConfig;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.HttpClient;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.HttpResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试HTTPServer
 *
 * @author zhp
 * @since 2022-08-02
 */
public class MetricServiceImplTest {

    private static MetricServiceImpl metricServiceImpl;

    private static final String APP_NAME_CODE = "appName";

    private static final String APP_NAME_VALUE = "test";

    private static final String ADDRESS = "127.0.0.1";

    private static final int PORT = 12345;

    private static final int SUCCESS_CODE = 200;

    private static final String URL = "http://127.0.0.1:12345";

    private static MonitorConfig monitorConfig = new MonitorConfig();

    private MockedStatic<ConfigManager> configManagerMockedStatic;

    /**
     * 配置转换器
     */
    @Before
    public void setUp() {
        monitorConfig.setAddress(ADDRESS);
        monitorConfig.setPort(PORT);
        monitorConfig.setStartMonitor(true);
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic
                .when(() -> ConfigManager.getConfig(MonitorConfig.class))
                .thenReturn(monitorConfig);
    }

    @After
    public void tearDown() {
        configManagerMockedStatic.close();
    }

    /**
     * 测试HttpServer
     */
    @Test
    public void testServerSuccess() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put(APP_NAME_CODE, APP_NAME_VALUE);
        metricServiceImpl = new MetricServiceImpl();
        metricServiceImpl.initMonitorServer();
        HttpClient httpClient = new DefaultHttpClient();
        HttpResult result = httpClient.doGet(URL);
        Assert.assertEquals(result.getCode(), SUCCESS_CODE);
    }

    /**
     * 测试HttpServer--开关关闭
     */
    @Test
    public void testServerClose() {
        monitorConfig.setStartMonitor(false);
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put(APP_NAME_CODE, APP_NAME_VALUE);
        metricServiceImpl = new MetricServiceImpl();
        metricServiceImpl.initMonitorServer();
        HttpClient httpClient = new DefaultHttpClient();
        HttpResult result = httpClient.doGet(URL);
        Assert.assertEquals(result.getCode(), HttpResult.ERROR_CODE);
    }
}
