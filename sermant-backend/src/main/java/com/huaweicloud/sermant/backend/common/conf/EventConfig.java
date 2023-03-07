/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.common.conf;

import com.huaweicloud.sermant.backend.dao.DatabaseType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 事件配置
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
@Configuration
public class EventConfig {

    /**
     * 数据库类型
     */
    @Value("${database.type}")
    private String database;

    /**
     * 数据库地址
     */
    @Value("${database.url}")
    private String url;

    /**
     * 数据库端口
     */
    @Value("${database.port}")
    private String port;

    @Value("${dabase.password}")
    private String password;

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DatabaseType getDatabase() {
        return DatabaseType.valueOf(database.toUpperCase(Locale.ROOT));
    }

    public void setDatabase(String db) {
        this.database = db;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
