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
public class BackendConfig {

    /**
     * 数据库类型
     */
    @Value("${database.type}")
    private String database;

    /**
     * 数据库地址
     */
    @Value("${database.address}")
    private String url;

    @Value("${database.user}")
    private String user;

    @Value("${database.password}")
    private String password;

    @Value("${database.expire}")
    private String expire;

    @Value("${webhook.eventpush.level}")
    private String webhookPushEventThreshold;

    @Value("${database.version}")
    private String version;

    @Value("${database.max.total}")
    private String maxTotal;

    @Value("${database.max.idle}")
    private String maxIdle;

    @Value("${database.timeout}")
    private String timeout;

    @Value("${session.expire}")
    private String sessionTimeout;

    @Value("${database.filter.thread.num}")
    private String filterThreadNum;

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

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public void setWebhookPushEventThreshold(String webhookPushEventThreshold) {
        this.webhookPushEventThreshold = webhookPushEventThreshold;
    }

    public String getWebhookPushEventThreshold() {
        return webhookPushEventThreshold;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(String maxIdle) {
        this.maxIdle = maxIdle;
    }

    public String getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(String maxTotal) {
        this.maxTotal = maxTotal;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getFilterThreadNum() {
        return filterThreadNum;
    }

    public void setFilterThreadNum(String filterThreadNum) {
        this.filterThreadNum = filterThreadNum;
    }
}
