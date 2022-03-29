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

package com.huawei.sermant.stresstest.db.mybatis;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.stresstest.config.ConfigFactory;
import com.huawei.sermant.stresstest.config.bean.DataSourceInfo;
import com.huawei.sermant.stresstest.core.Reflection;
import com.huawei.sermant.stresstest.core.Tester;
import com.huawei.sermant.stresstest.db.factory.ShadowFactory;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * DataSource扩展类，正常线程操作直接使用原数据库连接，压测线程操作使用影子库数据连接。
 *
 * @author yiwei
 * @since 2021-20-22
 */
@SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpSingleline"})
public class ShadowDataSource implements DataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String JDBC_PREFIX = "jdbc:";
    /**
     * static map, 重用已创建的shadow data source，避免资源浪费。
     */
    private static final Map<String, DataSource> ROUTING_DATA_SOURCE_MAP = new ConcurrentHashMap<>();

    private final DataSource dataSource;

    private String url;

    /**
     * 构造方法
     *
     * @param dataSource dataSource
     */
    public ShadowDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            this.url = parseDataSourceUrl(dataSource);
        } catch (Exception e) {
            this.url = "";
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (Tester.isTest()) {
            return getShadowDataSource().getConnection();
        }
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (Tester.isTest()) {
            return getShadowDataSource().getConnection(username, password);
        }
        return dataSource.getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (Tester.isTest()) {
            return getShadowDataSource().unwrap(iface);
        }
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (Tester.isTest()) {
            return getShadowDataSource().isWrapperFor(iface);
        }
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        if (Tester.isTest()) {
            getShadowDataSource().setLoginTimeout(seconds);
            return;
        }
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        if (Tester.isTest()) {
            return getShadowDataSource().getLoginTimeout();
        }
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    /**
     * 获取DataSource的url
     *
     * @return url
     */
    private String parseDataSourceUrl(DataSource newDataSource) throws Exception {
        Method method;
        try {
            method = newDataSource.getClass().getMethod("getUrl");
        } catch (NoSuchMethodException e) {
            method = newDataSource.getClass().getMethod("getJdbcUrl");
        }
        Object result = method.invoke(newDataSource);
        if (result instanceof String) {
            return getDbInfo((String)result);
        }
        return "";
    }

    private DataSource getShadowDataSource() {
        if (this.dataSource instanceof AbstractRoutingDataSource) {
            return getShadowRoutingDataSource((AbstractRoutingDataSource)dataSource);
        }
        return getShadowDataSourcebyUrl(url, dataSource);
    }

    private DataSource getShadowDataSourcebyUrl(String newUrl, DataSource source) {
        DataSource shadowDataSource = ROUTING_DATA_SOURCE_MAP.get(newUrl);
        if (shadowDataSource == null) {
            DataSourceInfo info = ConfigFactory.getConfig().getShadowDataSourceInfo(newUrl);
            if (info == null) {
                LOGGER.warning(String.format(Locale.ROOT, "Using original datasource %s on stress request", newUrl));
                return source;
            }
            shadowDataSource = ShadowFactory.getShadowFactory(source)
                .map(shadow -> shadow.shadowDataSource(source, info)).orElse(source);
            ROUTING_DATA_SOURCE_MAP.put(newUrl, shadowDataSource);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format(Locale.ROOT, "Using shadow datasource %s",
                ConfigFactory.getConfig().getShadowDataSourceInfo(newUrl).getUrl()));
        }
        return shadowDataSource;
    }

    private DataSource getShadowRoutingDataSource(AbstractRoutingDataSource source) {
        return Reflection.invokeDeclared("determineTargetDataSource", source).map(target -> {
            if (target instanceof DataSource) {
                try {
                    String localUrl = parseDataSourceUrl((DataSource)target);
                    return getShadowDataSourcebyUrl(localUrl, (DataSource)target);
                } catch (Exception e) {
                    LOGGER.severe(String.format(Locale.ROOT, "Cannot shadow %s.", target));
                }
            }
            return source;
        }).orElse(source);
    }

    private String getDbInfo(String urlString) {
        try {
            if (!urlString.startsWith(JDBC_PREFIX)) {
                return urlString;
            }
            URI uri = new URI(urlString.substring(JDBC_PREFIX.length()));
            return uri.getAuthority() + uri.getPath();
        } catch (URISyntaxException e) {
            LOGGER.warning(String.format(Locale.ROOT, "Cannot parse url:%s.", urlString));
        }
        return urlString;
    }

}
