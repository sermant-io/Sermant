/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.server.storage.plugin.elasticsearch7;

import com.huawei.apm.core.query.dao.IDruidQueryDao;
import com.huawei.apm.core.query.dao.IServerMonitorQueryDao;

import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.StorageModuleElasticsearch7Provider;

/**
 * 集成原有的es7实现，增加{@link IDruidQueryDao}注册
 *
 * @author zhouss
 * @since 2020-12-04
 */
public class StorageModuleElasticsearch7ProviderExtension extends StorageModuleElasticsearch7Provider {
    @Override
    public void prepare() throws ServiceNotProvidedException {
        super.prepare();
        this.registerServiceImplementation(IDruidQueryDao.class, new DruidQueryEs7DAO(elasticSearch7Client));
        this.registerServiceImplementation(IServerMonitorQueryDao.class,
            new ServerMonitorQueryEs7DAO(elasticSearch7Client));
    }
}
