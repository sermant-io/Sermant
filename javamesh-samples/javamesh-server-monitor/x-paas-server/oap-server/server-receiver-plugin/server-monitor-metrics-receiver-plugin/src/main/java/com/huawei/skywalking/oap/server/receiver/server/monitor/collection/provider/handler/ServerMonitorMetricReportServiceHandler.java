/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.server.monitor.collection.provider.handler;

import com.huawei.apm.core.dispatcher.ServerMonitorMetricDispatcher;
import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.ServerMonitoringMetricReplicator;
import com.huawei.apm.network.language.agent.v3.ServerMonitoringCollection;
import com.huawei.apm.network.language.agent.v3.ServerMonitoringMetricReportServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.apache.skywalking.apm.network.common.v3.Commands;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.grpc.GRPCHandler;

/**
 * grpc方式接收serverMonitor采集的数据
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
@Slf4j
public class ServerMonitorMetricReportServiceHandler extends
        ServerMonitoringMetricReportServiceGrpc.ServerMonitoringMetricReportServiceImplBase implements GRPCHandler {
    private final ServerMonitorMetricDispatcher monitorMetricDispatcher;
    private final NamingControl namingControl;

    public ServerMonitorMetricReportServiceHandler(ModuleManager moduleManager) {
        this.monitorMetricDispatcher = new ServerMonitorMetricDispatcher(moduleManager);
        this.namingControl = moduleManager
                .find(CoreModule.NAME)
                .provider()
                .getService(NamingControl.class);
    }

    @Override
    public void collect(ServerMonitoringCollection request, StreamObserver<Commands> responseObserver) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "receive the serverMonitor metrics from service instance, name: {}, instance: {}",
                    request.getService(),
                    request.getServiceInstance()
            );
        }
        final ServerMonitoringCollection.Builder builder = request.toBuilder();
        builder.setService(namingControl.formatServiceName(builder.getService()));
        builder.setServiceInstance(namingControl.formatInstanceName(builder.getServiceInstance()));
        builder.getMetricList().forEach(monitorMetric -> {
            monitorMetricDispatcher.sendMetric(builder.getService(), builder.getServiceInstance(), monitorMetric, BaseReplicator.NO_COPY);
        });
        // huawei update.无损演练：修改复制标签值
        if (CoreModuleConfig.losslessDrillSwitchStatus && builder.getCopy() == BaseReplicator.TO_COPY) {
            ServerMonitoringMetricReplicator.copyModification(builder);
            builder.getMetricList().forEach(monitorMetric -> {
                monitorMetricDispatcher.sendMetric(builder.getService(), builder.getServiceInstance(), monitorMetric, BaseReplicator.TO_COPY);
            });
        }
        responseObserver.onNext(Commands.newBuilder().build());
        responseObserver.onCompleted();
    }
}
