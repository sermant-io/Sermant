/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo.config;

import java.util.Map;

/**
 * aggregate configurations for apollo.
 *
 * @author Chen Zhenyang
 * @since 2025-08-14
 */
public class ApolloProperty {

    private static volatile String appId;
    private static volatile String cluster;
    private static volatile String env;
    private static volatile String url;

    // authentication for config service, users can choose to open
    private static volatile String accessKey;

    // url for open api
    private static volatile String adminUrl;

    // authentication for open api, which is necessary
    private static volatile String token;
    private static volatile String user;

    // check if the publish operation must be performed by a user different from the editor
    private static volatile boolean doubleCheck;
    private static volatile int readTimeout;
    private static volatile int connectTimeout;

    /**
     * constructor
     */
    private ApolloProperty() {
    }

    /**
     * set apollo meta configs
     *
     * @param params apollo config params
     */
    public static void setProperties(Map<String,String> params) {
        appId = params.get("app_id");
        cluster = params.get("cluster");
        env = params.get("env");
        url = params.get("url");
        accessKey = params.get("access_key");
        token = params.get("token");
        readTimeout = Integer.parseInt(params.get("read_timeout"));
        connectTimeout = Integer.parseInt(params.get("connect_timeout"));
        adminUrl = params.get("admin_url");
        user = params.get("user");
        doubleCheck = Boolean.parseBoolean(params.get("double_check"));
    }

    public static int getReadTimeout() {
        return readTimeout;
    }

    public static int getConnectTimeout() {
        return connectTimeout;
    }

    public static String getToken() {
        return token;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    public static String getAppId() {
        return appId;
    }

    public static String getCluster() {
        return cluster;
    }

    public static String getUser() {
        return user;
    }

    public static String getUrl() {
        return url;
    }

    public static String getEnv() {
        return env;
    }

    public static String getAdminUrl() {
        return adminUrl;
    }

    public static boolean isDoubleCheck() {
        return doubleCheck;
    }
}
