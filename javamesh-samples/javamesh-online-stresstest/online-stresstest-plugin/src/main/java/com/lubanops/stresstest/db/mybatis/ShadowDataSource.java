/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.db.mybatis;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.config.bean.DataSourceInfo;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.core.Tester;
import com.lubanops.stresstest.db.factory.ShadowFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DataSource扩展类，正常线程操作直接使用原数据库连接，压测线程操作使用影子库数据连接。
 *
 * @author yiwei
 * @since 2021/20/22
 */
public class ShadowDataSource implements DataSource {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String JDBC_PREFIX = "jdbc:";
    /**
     * static map, 重用已创建的shadow data source，避免资源浪费。
     */
    private static final Map<String, DataSource> ROUTING_DATA_SOURCE_MAP = new ConcurrentHashMap<>();

    private final DataSource dataSource;

    private String url;

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
    private String parseDataSourceUrl(DataSource dataSource) throws Exception {
        Method method;
        try {
            method = dataSource.getClass().getMethod("getUrl");
        } catch (NoSuchMethodException e) {
            method = dataSource.getClass().getMethod("getJdbcUrl");
        }
        Object result = method.invoke(dataSource);
        if (result instanceof String) {
            return getDBInfo((String) result);
        }
        return "";
    }

    private DataSource getShadowDataSource() {
        if (this.dataSource instanceof AbstractRoutingDataSource) {
            return getShadowRoutingDataSource((AbstractRoutingDataSource) dataSource);
        }
        return getShadowDataSourcebyUrl(url, dataSource);
    }

    private DataSource getShadowDataSourcebyUrl(String url, DataSource source) {
        DataSource shadowDataSource = ROUTING_DATA_SOURCE_MAP.get(url);
        if (shadowDataSource == null) {
            DataSourceInfo info = ConfigFactory.getConfig().getShadowDataSourceInfo(url);
            if (info == null) {
                LOGGER.warning(String.format("Using original datasource %s on stress request", url));
                return source;
            }
            shadowDataSource = ShadowFactory.getShadowFactory(source)
                    .map(shadow -> shadow.shadowDataSource(source, info)).orElse(source);
            ROUTING_DATA_SOURCE_MAP.put(url, shadowDataSource);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Using shadow datasource %s", ConfigFactory.getConfig()
                    .getShadowDataSourceInfo(url).getUrl()));
        }
        return shadowDataSource;
    }

    private DataSource getShadowRoutingDataSource(AbstractRoutingDataSource source) {
        return Reflection.invokeDeclared("determineTargetDataSource", source).map(target -> {
            if (target instanceof DataSource) {
                try {
                    String localUrl = parseDataSourceUrl((DataSource) target);
                    return getShadowDataSourcebyUrl(localUrl, (DataSource) target);
                } catch (Exception e) {
                    LOGGER.severe(String.format("Cannot shadow %s.", target));
                }
            }
            return source;
        }).orElse(source);
    }

    private String getDBInfo(String urlString) {
        try {
            if (!urlString.startsWith(JDBC_PREFIX)) {
                return urlString;
            }
            URI uri = new URI(urlString.substring(JDBC_PREFIX.length()));
            return uri.getAuthority()  + uri.getPath();
        } catch (URISyntaxException e) {
            LOGGER.warning(String.format("Cannot parse url:%s.", urlString));
        }
        return urlString;
    }

}
