/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.skywalking.oap.server.receiver.connection.pool.service;

import com.huawei.apm.core.query.NodeCondition;
import com.huawei.apm.core.query.NodeRecords;
import com.huawei.apm.core.query.dao.IDruidQueryDao;

import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.Service;

import java.io.IOException;

/**
 * druid查询接口，可查询应用，实例，数据源信息
 *
 * @author zhouss
 * @since 2020-11-29
 */
public class DruidQueryService implements Service {
    private final ModuleManager moduleManager;

    private IDruidQueryDao druidQueryDao;

    public DruidQueryService(ModuleManager manager) {
        this.moduleManager = manager;
    }

    private IDruidQueryDao getDruidQueryDao() {
        if (druidQueryDao == null) {
            this.druidQueryDao = moduleManager.find(StorageModule.NAME).provider().getService(IDruidQueryDao.class);
        }
        return druidQueryDao;
    }

    /**
     * 节点信息查询
     *
     * @param condition 查询条件，时间范围，指定字段查询
     * @return 节点记录
     * @throws IOException 查询异常,针对数据库sql执行异常
     */
    public NodeRecords queryNodeRecords(NodeCondition condition) throws IOException {
        return getDruidQueryDao().queryNodeRecords(condition);
    }
}
