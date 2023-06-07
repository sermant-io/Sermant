/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.intergration.monitor;

import com.huaweicloud.intergration.common.MetricEnum;
import com.huaweicloud.intergration.common.utils.RequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控测试类
 *
 * @author ZHP
 * @since 2002-11-24
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "MONITOR")
public class MonitorTest {
    /**
     * 监控采集URL
     */
    private static final String URL = "http://127.0.0.1:12345/";

    private static final String REQ_URL = "http://127.0.0.1:8015/flowcontrol/timedBreaker";

    private static final String QPS = "qps";

    private static final String TPS = "tps";

    @Test
    public void testMonitor() {
        String string = RequestUtils.get(URL, new HashMap<>(), String.class);
        Assertions.assertNotNull(string, "指标信息查询失败");
        String[] metrics = string.split("\n");
        Map<String, Double> map = new HashMap<>();
        for (String metric : metrics) {
            if (metric.startsWith("#")) {
                continue;
            }
            String[] data = metric.split(" ");
            if (data.length >= 2) {
                map.put(data[0], Double.parseDouble(data[1]));
            }
        }
        Assertions.assertFalse(map.isEmpty(), "解析响应结果获取指标信息失败");
        for (MetricEnum metricEnum : MetricEnum.values()) {
            Assertions.assertTrue(map.containsKey(metricEnum.getName()), "缺少指标信息" + metricEnum.getName());
        }
    }

    @Test
    public void testFlowControlMonitor() {
        String res = RequestUtils.get(REQ_URL, new HashMap<>(), String.class);
        Assertions.assertNotNull(res, "熔断指标前置请求失败");
        String string = RequestUtils.get(URL, new HashMap<>(), String.class);
        Assertions.assertNotNull(string, "熔断指标信息查询失败");
        String[] metrics = string.split("\n");
        boolean qpsFlag = false;
        boolean tpsFlag = false;
        for (String metric : metrics) {
            if (metric.startsWith("#")) {
                continue;
            }
            String[] data = metric.split(" ");
            if (data[0].startsWith(QPS)) {
                qpsFlag = true;
            }
            if (data[0].startsWith(TPS)) {
                tpsFlag = true;
            }
        }
        Assertions.assertTrue(qpsFlag, "缺少qps指标信息");
        Assertions.assertTrue(tpsFlag, "缺少tps指标信息");
    }
}
