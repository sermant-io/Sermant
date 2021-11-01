/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.db.factory;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.lubanops.stresstest.config.bean.DataSourceInfo;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * 生成tomcat 影子datasource
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class ShadowTomcatDataSource implements Shadow {
    private static final Logger LOGGER = LogFactory.getLogger();

    @Override
    public DataSource shadowDataSource(DataSource source, DataSourceInfo shadowInfo) {
        org.apache.tomcat.jdbc.pool.DataSource original = (org.apache.tomcat.jdbc.pool.DataSource)source;
        org.apache.tomcat.jdbc.pool.DataSource shadowDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        shadowDataSource.setUrl(shadowInfo.getUrl());
        StringBuilder builder = new StringBuilder();
        if (!StringUtils.isBlank(shadowInfo.getUserPrefix())) {
            builder.append(shadowInfo.getUserPrefix());
        }
        builder.append(original.getUsername());
        if (!StringUtils.isBlank(shadowInfo.getUserSuffix())) {
            builder.append(shadowInfo.getUserSuffix());
        }
        shadowDataSource.setUsername(builder.toString());
        shadowDataSource.setPassword(original.getPassword());
        LOGGER.fine(String.format("Use shadow tomcat jdbc url:%s", shadowDataSource.getUrl()));
        shadowDataSource.setDriverClassName(original.getDriverClassName());
        shadowDataSource.setCommitOnReturn(original.getCommitOnReturn());
        shadowDataSource.setAlternateUsernameAllowed(original.isAlternateUsernameAllowed());
        shadowDataSource.setAccessToUnderlyingConnectionAllowed(original.isAccessToUnderlyingConnectionAllowed());
        shadowDataSource.setAbandonWhenPercentageFull(original.getAbandonWhenPercentageFull());
        shadowDataSource.setMinIdle(original.getMinIdle());
        shadowDataSource.setMaxWait(original.getMaxWait());
        shadowDataSource.setMaxAge(original.getMaxAge());
        shadowDataSource.setMaxActive(original.getMaxActive());
        shadowDataSource.setMaxIdle(original.getMaxIdle());
        shadowDataSource.setValidationInterval(original.getValidationInterval());
        shadowDataSource.setValidationQuery(original.getValidationQuery());
        shadowDataSource.setValidationQueryTimeout(original.getValidationQueryTimeout());
        shadowDataSource.setInitialSize(original.getInitialSize());
        shadowDataSource.setTestWhileIdle(original.isTestWhileIdle());
        shadowDataSource.setTestOnBorrow(original.isTestOnBorrow());
        shadowDataSource.setDefaultAutoCommit(original.getDefaultAutoCommit());
        shadowDataSource.setDefaultReadOnly(original.getDefaultReadOnly());
        shadowDataSource.setDefaultTransactionIsolation(original.getDefaultTransactionIsolation());
        shadowDataSource.setDefaultCatalog(original.getDefaultCatalog());
        shadowDataSource.setTestOnReturn(original.isTestOnReturn());
        shadowDataSource.setMinEvictableIdleTimeMillis(original.getMinEvictableIdleTimeMillis());
        shadowDataSource.setTimeBetweenEvictionRunsMillis(original.getTimeBetweenEvictionRunsMillis());
        shadowDataSource.setAlternateUsernameAllowed(original.isAlternateUsernameAllowed());
        shadowDataSource.setConnectionProperties(original.getConnectionProperties());
        try {
            shadowDataSource.createPool();
        } catch (SQLException e) {
            LOGGER.severe(String.format("Create connection pool error: %s.", e.getMessage()));
        }
        return shadowDataSource;
    }
}
