/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.connection.pool.provider;

import com.huawei.skywalking.oap.server.receiver.connection.pool.define.ConnectionPoolOALDefine;
import com.huawei.skywalking.oap.server.receiver.connection.pool.handler.ConnectionPoolMetricReportServiceHandler;
import com.huawei.skywalking.oap.server.receiver.connection.pool.module.ConnectionPoolModule;
import com.huawei.skywalking.oap.server.receiver.connection.pool.service.DruidQueryService;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.oal.rt.OALEngineLoaderService;
import org.apache.skywalking.oap.server.core.server.GRPCHandlerRegister;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.receiver.sharing.server.SharingServerModule;

/**
 * 数据源指标GRPC接收模块入口类
 *
 * @author zhouss
 * @since 2020-12-03
 */
public class ConnectionPoolModuleProvider extends ModuleProvider {
    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return ConnectionPoolModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return new ConnectionPoolModuleConfig();
    }

    @Override
    public void prepare() {
        this.registerServiceImplementation(DruidQueryService.class, new DruidQueryService(getManager()));
    }

    @Override
    public void start() throws ModuleStartException {
        getManager()
                .find(CoreModule.NAME)
                .provider()
                .getService(OALEngineLoaderService.class)
                .load(ConnectionPoolOALDefine.INSTANCE);

        GRPCHandlerRegister grpcHandlerRegister =
                getManager().find(SharingServerModule.NAME).provider().getService(GRPCHandlerRegister.class);
        grpcHandlerRegister.addHandler(new ConnectionPoolMetricReportServiceHandler(getManager()));
    }

    @Override
    public void notifyAfterCompleted() {
    }

    @Override
    public String[] requiredModules() {
        return new String[] {CoreModule.NAME, SharingServerModule.NAME};
    }
}
