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

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Druid 影子datasrouce
 *
 * @author yiwei
 * @since 2021-10-21
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
        LOGGER.fine(String.format(Locale.ROOT, "Use druid shadow url:%s", shadowSource.getUrl()));
        shadowSource.setDriverClassName(original.getDriverClassName());
        shadowSource.setInitialSize(original.getInitialSize());
        shadowSource.setMinIdle(original.getMinIdle());
        shadowSource.setMaxActive(original.getMaxActive());
        shadowSource.setMaxWait(original.getMaxWait());
        try {
            shadowSource.init();
        } catch (SQLException throwable) {
            LOGGER.severe(String.format(Locale.ROOT, "Init shadow druid source error: %s", throwable.getMessage()));
        }
        return shadowSource;
    }
}
