/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.db.factory;


import com.huawei.javamesh.core.common.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 影子工厂
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class ShadowFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 生成影子datasource工厂
     *
     * @param source 原始的datasource
     * @return 对应的datasource 工厂
     */
    public static Optional<Shadow> getShadowFactory(DataSource source) {
        Class<?> clazz = DataSourceType.map.get(source.getClass().getName());
        try {
            Object instance = clazz.newInstance();
            if (instance instanceof Shadow) {
                return Optional.of((Shadow) instance);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.severe(String.format("Cannot new %s", clazz.getName()));
        }
        return Optional.empty();
    }

    enum DataSourceType {
        DRUID("com.alibaba.druid.pool.DruidDataSource", ShadowDruid.class),
        HIKARI("com.zaxxer.hikari.HikariDataSource", ShadowHikariDataSource.class),
        TOMCAT_JDBC("org.apache.tomcat.jdbc.pool.DataSource", ShadowTomcatDataSource.class);

        DataSourceType(String dataSourceClazz, Class<?> shadowFactoryClazz) {
            this.dataSourceClazz = dataSourceClazz;
            this.shadowFactoryClazz = shadowFactoryClazz;
        }

        String dataSourceClazz;
        Class<?> shadowFactoryClazz;

        static Map<String, Class<?>> map = new HashMap<>();

        static {
            for (DataSourceType type: DataSourceType.values()) {
                map.put(type.dataSourceClazz, type.shadowFactoryClazz);
            }
        }
    }
}
