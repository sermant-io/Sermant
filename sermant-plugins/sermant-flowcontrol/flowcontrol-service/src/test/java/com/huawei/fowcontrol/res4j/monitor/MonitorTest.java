/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.fowcontrol.res4j.monitor;

import com.huawei.flowcontrol.common.entity.MetricEntity;
import com.huawei.flowcontrol.common.enums.MetricType;
import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.fowcontrol.res4j.service.ServiceCollectorService;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控测试类
 *
 * @author zhp
 * @since 2022-09-15
 */
public class MonitorTest {
    private static final String FILED_NAME = "monitors";

    private static final long DEFAULT_VALUE = 1000L;

    private static final String NAME = "default";

    private static final String COLLECT_FUSE_METRIC_NAME = "collectFuseMetric";

    private static final String COPY_METHOD_NAME = "copy";

    private static final String COPY_VALUE_METHOD_NAME = "copyValue";

    private static final String CURRENT_MAP_METHOD_NAME = "getCurrentMap";

    @Test
    public void testCollect() {
        ServiceCollectorService service = new ServiceCollectorService();
        List<Collector.MetricFamilySamples> metricFamilySamplesList = service.collect();
        if (metricFamilySamplesList != null && !metricFamilySamplesList.isEmpty()
                && ServiceCollectorService.CIRCUIT_BREAKER_MAP.isEmpty()) {
            metricFamilySamplesList.forEach(metricFamilySamples -> metricFamilySamples.samples.forEach(sample -> {
                Assert.assertEquals(0, sample.value, 0.0);
                Assert.assertNotNull(sample.name);
            }));
        }
        Map<String, MetricEntity> monitors = new ConcurrentHashMap<>();
        MetricEntity metricEntity = new MetricEntity();
        metricEntity.getFuseRequest().getAndAdd(DEFAULT_VALUE);
        monitors.put(NAME, metricEntity);
        ReflectUtils.setFieldValue(service, FILED_NAME, monitors);
        Assert.assertNotNull(metricFamilySamplesList);
        metricFamilySamplesList.forEach(metricFamilySamples -> metricFamilySamples.samples.forEach(sample -> {
            if (StringUtils.equal(sample.name, MetricType.FUSED_REQUEST.getName())) {
                Assert.assertEquals(sample.value, DEFAULT_VALUE);
            }
        }));
    }

    @Test
    public void testCollectFuseMetric() {
        Map<String, GaugeMetricFamily> map = new ConcurrentHashMap<>();
        ReflectUtils.invokeMethod(new ServiceCollectorService(), COLLECT_FUSE_METRIC_NAME,
                new Class[]{Map.class, String.class, MetricEntity.class}, new Object[]{map, NAME, new MetricEntity()});
        Assert.assertFalse(map.isEmpty());
        map.forEach((k, v) -> {
            Assert.assertNotNull(k);
            Assert.assertTrue(v != null && v.samples != null);
            v.samples.forEach(sample -> {
                Assert.assertNotNull(sample.name);
                Assert.assertEquals(0.0, sample.value, 0.0);
            });
        });
    }

    @Test
    public void testCopy() {
        MetricEntity metricEntity = new MetricEntity();
        MetricEntity sourceMetric = new MetricEntity();
        sourceMetric.getServerRequest().set(DEFAULT_VALUE);
        ReflectUtils.invokeMethod(new ServiceCollectorService(), COPY_METHOD_NAME, new Class[]{MetricEntity.class,
                MetricEntity.class}, new Object[]{metricEntity, sourceMetric});
        Assert.assertEquals(metricEntity.getServerRequest().get(), sourceMetric.getServerRequest().get());
        AtomicLong atomicLong = new AtomicLong();
        AtomicLong sourceAtomic = new AtomicLong(DEFAULT_VALUE);
        ReflectUtils.invokeMethod(new ServiceCollectorService(), COPY_VALUE_METHOD_NAME, new Class[]{AtomicLong.class,
                AtomicLong.class}, new Object[]{atomicLong, sourceAtomic});
        Assert.assertEquals(atomicLong.get(), sourceAtomic.get());
    }

    @Test
    public void testGetCurrent() {
        MetricEntity metricEntity = new MetricEntity();
        metricEntity.getServerRequest().set(DEFAULT_VALUE);
        Map<String, MetricEntity> entityMap = new HashMap<>();
        entityMap.put(NAME, metricEntity);
        Optional<Object> optional = ReflectUtils.invokeMethod(new ServiceCollectorService(), CURRENT_MAP_METHOD_NAME,
                new Class[]{Map.class},
                new Object[]{entityMap});
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(optional.get() instanceof Map);
        Map<String, MetricEntity> targetMap = (Map<String, MetricEntity>) optional.get();
        MetricEntity entity = targetMap.get(NAME);
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getServerRequest().get(), metricEntity.getServerRequest().get());
    }
}
