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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 影子工厂
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class ShadowFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private ShadowFactory() {
    }

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
                return Optional.of((Shadow)instance);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.severe(String.format(Locale.ROOT, "Cannot new %s", clazz.getName()));
        }
        return Optional.empty();
    }

    /**
     * DataSourceType
     *
     * @since 2021-10-21
     */
    enum DataSourceType {
        DRUID("com.alibaba.druid.pool.DruidDataSource", ShadowDruid.class),
        HIKARI("com.zaxxer.hikari.HikariDataSource", ShadowHikariDataSource.class),
        TOMCAT_JDBC("org.apache.tomcat.jdbc.pool.DataSource", ShadowTomcatDataSource.class);

        static Map<String, Class<?>> map = new HashMap<>();

        String dataSourceClazz;

        Class<?> shadowFactoryClazz;

        DataSourceType(String dataSourceClazz, Class<?> shadowFactoryClazz) {
            this.dataSourceClazz = dataSourceClazz;
            this.shadowFactoryClazz = shadowFactoryClazz;
        }

        static {
            for (DataSourceType type : DataSourceType.values()) {
                map.put(type.dataSourceClazz, type.shadowFactoryClazz);
            }
        }
    }
}
