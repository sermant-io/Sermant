/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/jvm/JVMService.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.plugin.servermonitor.provider;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import com.huawei.sermant.plugin.monitor.common.collect.MetricProvider;
import com.huawei.sermant.plugin.monitor.common.config.ServiceConfig;
import org.apache.skywalking.apm.agent.core.jvm.cpu.CPUProvider;
import org.apache.skywalking.apm.agent.core.jvm.gc.GCProvider;
import org.apache.skywalking.apm.agent.core.jvm.memory.MemoryProvider;
import org.apache.skywalking.apm.agent.core.jvm.memorypool.MemoryPoolProvider;
import org.apache.skywalking.apm.agent.core.jvm.thread.ThreadProvider;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * OpenJdk JVM Metric Provider
 */
public class OpenJvmMetricProvider implements MetricProvider<JVMMetric> {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int GATEWAY_DATA_TYPE = 5;
    private static final OpenJvmMetricProvider INSTANCE = new OpenJvmMetricProvider();

    private final GatewayClient gatewayClient;
    private final String service;
    private final String serviceInstance;

    private OpenJvmMetricProvider() {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
    }

    public static OpenJvmMetricProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public JVMMetric collect() {
        final long currentTimeMillis = System.currentTimeMillis();
        return JVMMetric.newBuilder().setTime(currentTimeMillis)
            .setCpu(CPUProvider.INSTANCE.getCpuMetric())
            .addAllMemory(MemoryProvider.INSTANCE.getMemoryMetricList())
            .addAllMemoryPool(MemoryPoolProvider.INSTANCE.getMemoryPoolMetricsList())
            .addAllGc(GCProvider.INSTANCE.getGCList())
            .setThread(ThreadProvider.INSTANCE.getThreadMetrics())
            .build();
    }

    @Override
    public void consume(List<JVMMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No OpenJdk jvm metric was collected.");
            return;
        }
        JVMMetricCollection collection = JVMMetricCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
