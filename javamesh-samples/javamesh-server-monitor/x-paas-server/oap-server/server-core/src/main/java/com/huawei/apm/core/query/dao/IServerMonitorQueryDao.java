/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.query.dao;

import com.huawei.apm.core.query.DiskIoMetric;
import com.huawei.apm.core.query.DiskQueryCondition;
import org.apache.skywalking.oap.server.core.storage.DAO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * serverMonitor查询接口
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
public interface IServerMonitorQueryDao extends DAO {
    Optional<List<DiskIoMetric>> queryDisk(DiskQueryCondition condition)  throws IOException;
}
