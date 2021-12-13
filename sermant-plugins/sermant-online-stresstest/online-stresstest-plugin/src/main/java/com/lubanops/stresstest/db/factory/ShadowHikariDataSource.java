/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.db.factory;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.lubanops.stresstest.config.bean.DataSourceInfo;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.logging.Logger;

/**
 * Hikari 影子datasource
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class ShadowHikariDataSource implements Shadow {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public DataSource shadowDataSource(DataSource source, DataSourceInfo shadowInfo) {
        HikariDataSource original = (HikariDataSource)source;
        HikariDataSource shadowSource = new HikariDataSource();
        shadowSource.setJdbcUrl(shadowInfo.getUrl());
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
        LOGGER.fine(String.format("Use shadow hikari url:%s", shadowSource.getJdbcUrl()));
        shadowSource.setDriverClassName(original.getDriverClassName());
        shadowSource.setMaximumPoolSize(original.getMaximumPoolSize());
        shadowSource.setMinimumIdle(original.getMinimumIdle());
        shadowSource.setMaxLifetime(original.getMaxLifetime());
        shadowSource.setAutoCommit(original.isAutoCommit());
        shadowSource.setConnectionTimeout(original.getConnectionTimeout());
        shadowSource.setIdleTimeout(original.getIdleTimeout());
        shadowSource.setConnectionTestQuery(original.getConnectionTestQuery());
        shadowSource.setInitializationFailTimeout(original.getInitializationFailTimeout());
        shadowSource.setConnectionInitSql(original.getConnectionInitSql());
        shadowSource.setCatalog(original.getCatalog());
        shadowSource.setReadOnly(original.isReadOnly());
        shadowSource.setIsolateInternalQueries(original.isIsolateInternalQueries());
        shadowSource.setTransactionIsolation(original.getTransactionIsolation());
        shadowSource.setValidationTimeout(original.getValidationTimeout());
        shadowSource.setLeakDetectionThreshold(original.getLeakDetectionThreshold());
        shadowSource.setSchema(original.getSchema());
        shadowSource.setThreadFactory(original.getThreadFactory());
        shadowSource.setScheduledExecutor(original.getScheduledExecutor());
        return shadowSource;
    }
}
