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

package com.huawei.monitor;

import com.huawei.monitor.service.collector.JvmCollectorService;
import com.huawei.monitor.service.collector.ServerCollectorService;
import com.huawei.monitor.util.CollectionUtil;

import com.huaweicloud.sermant.core.utils.StringUtils;

import io.prometheus.client.Collector;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 测试性能指标收集
 *
 * @author zhp
 * @since 2022-08-02
 */
public class CollectorServiceTest {

    private static final String OS_NAME_CODE = "os.name";

    private static final String LINUX__OS_NAME = "Linux";

    /**
     * 测试能否正常获取JVM性能指标
     */
    @Test
    public void testJvm() {
        JvmCollectorService jvmCollectorService = new JvmCollectorService();
        List<Collector.MetricFamilySamples> metricFamilySamplesList = jvmCollectorService.collect();
        Assert.assertFalse(CollectionUtil.isEmpty(metricFamilySamplesList));
        metricFamilySamplesList.forEach(samplesList -> {
            Assert.assertNotNull(samplesList);
            Assert.assertFalse(CollectionUtil.isEmpty(samplesList.samples));
            samplesList.samples.forEach(sample -> Assert.assertNotNull(sample.name));
        });
        String osName = System.getProperty(OS_NAME_CODE);
        if (!StringUtils.isEmpty(osName) && osName.startsWith(LINUX__OS_NAME)) {
            ServerCollectorService serverCollectorService = new ServerCollectorService();
            List<Collector.MetricFamilySamples> serverMetricFamilySamples = serverCollectorService.collect();
            Assert.assertFalse(CollectionUtil.isEmpty(serverMetricFamilySamples));
            serverMetricFamilySamples.forEach(samplesList -> {
                Assert.assertNotNull(samplesList);
                Assert.assertFalse(CollectionUtil.isEmpty(samplesList.samples));
                samplesList.samples.forEach(sample -> Assert.assertNotNull(sample.name));
            });
        }
    }
}
