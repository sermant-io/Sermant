/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.server.monitor.collection.provider;

import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.module.ServerMonitorModule;
import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.provider.handler.ServerMonitorMetricReportServiceHandler;
import com.huawei.skywalking.oap.server.receiver.server.monitor.collection.service.ServerMonitorQueryService;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.oal.rt.OALEngineLoaderService;
import org.apache.skywalking.oap.server.core.server.GRPCHandlerRegister;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.receiver.sharing.server.SharingServerModule;

/**
 * 服务器监控信息处理类
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
public class ServerMonitorModuleProvider extends ModuleProvider {
    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return ServerMonitorModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return new ServerMonitorModuleConfig();
    }

    @Override
    public void prepare() {
        this.registerServiceImplementation(ServerMonitorQueryService.class,
            new ServerMonitorQueryService(getManager()));
    }

    @Override
    public void start() throws ModuleStartException {
        getManager().find(CoreModule.NAME)
            .provider()
            .getService(OALEngineLoaderService.class)
            .load(ServerMonitorOalDefine.INSTANCE);

        GRPCHandlerRegister grpcHandlerRegister = getManager().find(SharingServerModule.NAME)
            .provider()
            .getService(GRPCHandlerRegister.class);
        grpcHandlerRegister.addHandler(new ServerMonitorMetricReportServiceHandler(getManager()));
    }

    @Override
    public void notifyAfterCompleted() {
    }

    @Override
    public String[] requiredModules() {
        return new String[]{
            CoreModule.NAME,
            SharingServerModule.NAME
        };
    }
}
