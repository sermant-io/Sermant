/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.server.analyzer.agent.kafka.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.apm.core.dispatcher.KafkaServerMonitorMetricDispatcher;
import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.KafkaServerMonitoringMetricReplicator;
import com.lubanops.apm.plugin.servermonitor.entity.ServerMonitorCollection;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.module.KafkaFetcherConfig;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.provider.handler.KafkaHandler;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

@Slf4j
public class ServerMonitorMetricHandler implements KafkaHandler {

    private final KafkaFetcherConfig config;

    private final KafkaServerMonitorMetricDispatcher monitorMetricDispatcher;
    private final NamingControl namingControl;


    public ServerMonitorMetricHandler(ModuleManager moduleManager,
                                      KafkaFetcherConfig config) {
        this.config = config;
        this.monitorMetricDispatcher = new KafkaServerMonitorMetricDispatcher(moduleManager);
        this.namingControl = moduleManager
            .find(CoreModule.NAME)
            .provider()
            .getService(NamingControl.class);
    }

    @Override
    public String getConsumePartitions() {
        return config.getConsumePartitions();
    }

    @Override
    public String getTopic() {
        return config.getTopicNameOfServerMonitor();
    }

    @Override
    public void handle(ConsumerRecord<String, Bytes> record) {
        try {
            ServerMonitorCollection serverMonitorCollection = ServerMonitorCollection.parseFrom(record.value().get());
            final ServerMonitorCollection.Builder builder = serverMonitorCollection.toBuilder();

            String service = namingControl.formatServiceName(builder.getService());
            String serviceInstance = namingControl.formatInstanceName(builder.getServiceInstance());
            builder.setService(service).setServiceInstance(serviceInstance);

            builder.getMetricList().forEach(monitorMetric -> {
                monitorMetricDispatcher.sendMetric(service, serviceInstance, monitorMetric, BaseReplicator.NO_COPY);
            });
            // huawei update.无损演练：修改复制标签值
            if (CoreModuleConfig.losslessDrillSwitchStatus && builder.getCopy() == BaseReplicator.TO_COPY) {
                KafkaServerMonitoringMetricReplicator.copyModification(builder);
                builder.getMetricList().forEach(monitorMetric -> {
                    monitorMetricDispatcher.sendMetric(service, serviceInstance, monitorMetric, BaseReplicator.TO_COPY);
                });
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("", e);
        }
    }
}
