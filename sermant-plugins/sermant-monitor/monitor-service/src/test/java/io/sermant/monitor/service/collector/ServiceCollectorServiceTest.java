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

package io.sermant.monitor.service.collector;

import io.prometheus.client.Collector;
import io.sermant.monitor.common.MetricCalEntity;
import io.sermant.monitor.common.MetricEnum;
import io.sermant.monitor.util.MonitorCacheUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * test microservice performance metric collection
 *
 * @author zhp
 * @since 2022-11-02
 */
public class ServiceCollectorServiceTest {
    private static final String NANE = "default";

    private static final int REQ_NUM = 20;

    private static final int REQ_TIME = 20;

    private static final String METRIC_NAME = MetricEnum.AVG_RESPONSE_TIME.getName();
    @Test
    public void testCollect() {
        ServiceCollectorService serviceCollectorService = new ServiceCollectorService();
        List<Collector.MetricFamilySamples> metricFamilySamplesList = serviceCollectorService.collect();
        Assert.assertTrue(metricFamilySamplesList.isEmpty());
        MetricCalEntity metricCalEntity = MonitorCacheUtil.getMetricCalEntity(NANE);
        serviceCollectorService.collect();
        metricCalEntity.getSuccessFulReqNum().addAndGet(REQ_NUM);
        metricCalEntity.getConsumeReqTimeNum().addAndGet(REQ_TIME);
        metricFamilySamplesList = serviceCollectorService.collect();
        Assert.assertFalse(metricFamilySamplesList.isEmpty());
        metricFamilySamplesList.forEach(metricFamilySamples -> {
            Assert.assertNotNull(metricFamilySamples.samples);
            Assert.assertEquals(1, metricFamilySamples.samples.size());
            if (METRIC_NAME.equals(metricFamilySamples.name)){
                Assert.assertEquals(metricFamilySamples.samples.get(0).value, (double) REQ_TIME / REQ_NUM, 0.0);
            }
        });
    }
}
