/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.connection.pool.handler;

import com.huawei.apm.core.dispatcher.ConnectionPoolMetricDispatcher;
import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.ConnectionPoolMetricReplicator;
import com.huawei.apm.core.source.ConnectionPoolType;
import com.huawei.apm.network.language.agent.v3.ConnectionPoolCollection;
import com.huawei.apm.network.language.agent.v3.ConnectionPoolMetricReporterServiceGrpc;

import io.grpc.stub.StreamObserver;

import org.apache.skywalking.apm.network.common.v3.Commands;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.grpc.GRPCHandler;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源指标  GRPC 接收类
 *
 * @author zhouss
 * @since 2020-12-03
 */
public class ConnectionPoolMetricReportServiceHandler
        extends ConnectionPoolMetricReporterServiceGrpc.ConnectionPoolMetricReporterServiceImplBase
        implements GRPCHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPoolMetricReportServiceHandler.class);

    /**
     * 数据源指标数据转发处理器
     */
    private final ConnectionPoolMetricDispatcher connectionPoolMetricDispatcher;

    public ConnectionPoolMetricReportServiceHandler(ModuleManager moduleManager) {
        this.connectionPoolMetricDispatcher = new ConnectionPoolMetricDispatcher(moduleManager);
    }

    @Override
    public void collect(ConnectionPoolCollection request, StreamObserver<Commands> responseObserver) {
        ConnectionPoolCollection.Builder builder = request.toBuilder();
        if (CollectionUtils.isNotEmpty(builder.getInstanceMetricsList())) {
            LOGGER.debug("received datasource metric size is {}", builder.getInstanceMetricsCount());
            builder.getInstanceMetricsBuilderList().forEach(connectionPoolMetricBuilder -> {
                connectionPoolMetricDispatcher.send(connectionPoolMetricBuilder.build(),
                        ConnectionPoolType.getConvertedPoolType(builder.getPoolTyp()), BaseReplicator.NO_COPY);
            });

            // huawei update.无损演练：修改复制标签值
            if (CoreModuleConfig.losslessDrillSwitchStatus && builder.getCopy() == BaseReplicator.TO_COPY) {
                ConnectionPoolMetricReplicator.copyModification(builder);
                builder.getInstanceMetricsBuilderList().forEach(connectionPoolMetricBuilder -> {
                    connectionPoolMetricDispatcher.send(connectionPoolMetricBuilder.build(),
                            ConnectionPoolType.getConvertedPoolType(builder.getPoolTyp()), BaseReplicator.TO_COPY);
                });
            }
        }
        responseObserver.onNext(Commands.newBuilder().build());
        responseObserver.onCompleted();
    }
}
