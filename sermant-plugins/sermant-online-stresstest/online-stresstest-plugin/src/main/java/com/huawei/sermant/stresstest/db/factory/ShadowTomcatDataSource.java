/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.stresstest.db.factory;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.utils.StringUtils;
import com.huawei.sermant.stresstest.config.bean.DataSourceInfo;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 生成tomcat 影子datasource
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class ShadowTomcatDataSource implements Shadow {
    private static final Logger LOGGER = LoggerFactory.getLogger();

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
        LOGGER.fine(String.format(Locale.ROOT, "Use shadow tomcat jdbc url:%s", shadowDataSource.getUrl()));
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
            LOGGER.severe(String.format(Locale.ROOT, "Create connection pool error: %s.", e.getMessage()));
        }
        return shadowDataSource;
    }
}
