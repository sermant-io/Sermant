/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.server.analyzer.agent.kafka.handler;

import com.huawei.apm.core.dispatcher.ConnectionPoolMetricDispatcher;
import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.ConnectionPoolMetricReplicator;
import com.huawei.apm.core.source.ConnectionPoolType;
import com.huawei.apm.network.language.agent.v3.ConnectionPoolCollection;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.module.KafkaFetcherConfig;
import org.apache.skywalking.oap.server.analyzer.agent.kafka.provider.handler.KafkaHandler;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.slf4j.Logger;

/**
 * 应用实例指标信息拉取处理器实现
 *
 * @author zhouss
 * @since 2020-11-28
 */
@Slf4j
public class ConnectionPoolMetricHandler implements KafkaHandler {
    private static final Logger LOGGER = log;

    private KafkaFetcherConfig config;

    private ConnectionPoolMetricDispatcher dispatcher;

    public ConnectionPoolMetricHandler(ModuleManager manager, KafkaFetcherConfig config) {
        this.config = config;
        this.dispatcher = new ConnectionPoolMetricDispatcher(manager);
    }

    @Override
    public void handle(ConsumerRecord<String, Bytes> record) {
        if (record != null) {
            ConnectionPoolCollection dataSourceCollection;
            try {
                dataSourceCollection = ConnectionPoolCollection.parseFrom(record.value().get());
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("convert record to metric failed! reason = {}", e.getMessage());
                return;
            }
            LOGGER.debug("consume the record, the metric count={}", dataSourceCollection.getInstanceMetricsCount());
            if (dataSourceCollection.getInstanceMetricsCount() > 0) {
                dataSourceCollection.getInstanceMetricsList().forEach(dataSourceMetric ->
                        dispatcher.send(dataSourceMetric,
                                ConnectionPoolType.getConvertedPoolType(dataSourceCollection.getPoolTyp()), BaseReplicator.NO_COPY)
                );
                // huawei update.无损演练：修改复制标签值
                if (CoreModuleConfig.losslessDrillSwitchStatus && dataSourceCollection.getCopy() == BaseReplicator.TO_COPY) {
                    ConnectionPoolCollection.Builder builder = dataSourceCollection.toBuilder();
                    ConnectionPoolMetricReplicator.copyModification(builder);
                    ConnectionPoolCollection copyDataSourceCollection = builder.build();
                    copyDataSourceCollection.getInstanceMetricsList().forEach(dataSourceMetric ->
                            dispatcher.send(dataSourceMetric,
                                    ConnectionPoolType.getConvertedPoolType(copyDataSourceCollection.getPoolTyp()), BaseReplicator.TO_COPY)
                    );
                }
            }
        }
    }

    @Override
    public String getConsumePartitions() {
        return config.getConsumePartitions();
    }

    @Override
    public String getTopic() {
        return config.getTopicNameOfDataSource();
    }
}
