/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.db.factory;

import com.lubanops.stresstest.config.bean.DataSourceInfo;

import javax.sql.DataSource;

/**
 * Shadow 数据库接口类
 *
 * @author yiwei
 * @since 2021/10/21
 */
public interface Shadow {
    /**
     * 生成影子的datasource
     * @param source 原始的datasource
     * @param shadowInfo 影子库信息
     * @return 影子datasource
     */
    DataSource shadowDataSource(DataSource source, DataSourceInfo shadowInfo);
}
