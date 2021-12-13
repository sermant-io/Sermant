/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.db.factory;

import com.alibaba.druid.pool.DruidDataSource;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.lubanops.stresstest.config.bean.DataSourceInfo;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Druid 影子datasrouce
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class ShadowDruid implements Shadow {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public DataSource shadowDataSource(DataSource source, DataSourceInfo shadowInfo) {
        DruidDataSource original = (DruidDataSource)source;
        DruidDataSource shadowSource = new DruidDataSource();
        shadowSource.setUrl(shadowInfo.getUrl());
        StringBuilder builder = new StringBuilder();
        if (!StringUtils.isBlank(shadowInfo.getUserPrefix())) {
            builder.append(shadowInfo.getUserPrefix());
        }
        builder.append(original.getUsername());
        if (!StringUtils.isBlank(shadowInfo.getUserSuffix())) {
            builder.append(shadowInfo.getUserSuffix());
        }
        shadowSource.setUsername(builder.toString());
        shadowSource.setPassword(original.getPassword());
        LOGGER.fine(String.format("Use druid shadow url:%s", shadowSource.getUrl()));
        shadowSource.setDriverClassName(original.getDriverClassName());
        shadowSource.setInitialSize(original.getInitialSize());
        shadowSource.setMinIdle(original.getMinIdle());
        shadowSource.setMaxActive(original.getMaxActive());
        shadowSource.setMaxWait(original.getMaxWait());
        try {
            shadowSource.init();
        } catch (SQLException throwable) {
            LOGGER.severe(String.format("Init shadow druid source error: %s", throwable.getMessage()));
        }
        return shadowSource;
    }
}
