/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.server.monitor.collection.service;

import com.huawei.apm.core.query.DiskIoMetric;
import com.huawei.apm.core.query.DiskQueryCondition;
import com.huawei.apm.core.query.dao.IServerMonitorQueryDao;

import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * server monitor查询服务
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
public class ServerMonitorQueryService implements Service {
    private final ModuleManager moduleManager;
    private IServerMonitorQueryDao monitorQueryDao;

    public ServerMonitorQueryService(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private IServerMonitorQueryDao getMonitorQueryDao() {
        if (monitorQueryDao == null) {
            this.monitorQueryDao = moduleManager
                .find(StorageModule.NAME)
                .provider()
                .getService(IServerMonitorQueryDao.class);
        }
        return monitorQueryDao;
    }

    /**
     * 查询disk信息
     *
     * @param condition 实体条件
     * @return 返回查询的实体内容
     * @throws IOException
     */
    public Optional<List<DiskIoMetric>> queryDisk(DiskQueryCondition condition) throws IOException {
        return getMonitorQueryDao().queryDisk(condition);
    }
}
