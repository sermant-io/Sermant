/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.hercules.controller.perftest;

/**
 * 功能描述：
 *
 * 
 * @since 2021-11-03
 */
public enum TaskInfoKey {
    TEST_ID("test_id", "id"),
    TEST_NAME("test_name", "testName"),
    TEST_TYPE("test_type", "scenarioType"),
    SCRIPT_PATH("script_path", "scriptName"),
    OWNER("owner", "userId"),
    START_TIME("start_time", "startTime"),
    DURATION("duration", "duration"),
    TPS("tps", "tps"),
    MTT("mtt", "meanTestTime"),
    FAIL_RATE("fail_rate", "errors"),
    DESC("desc", "description"),
    TOTAL("total", "total"),
    LABEL("label", "tagString"),
    STATUS_LABEL("status_label", "status"),
    STATUS("status", "status"),
    MONITORING_HOST("monitoring_hosts", "monitoringHosts");

    /**
     * 前端展示字段
     */
    private final String showKey;

    /**
     * 后端服务器响应字段
     */
    private final String serverKey;

    TaskInfoKey(String showName, String serverName) {
        this.showKey = showName;
        this.serverKey = serverName;
    }

    public String getShowKey() {
        return showKey;
    }

    public String getServerKey() {
        return serverKey;
    }

    /**
     * 根据serverKey获取前端展示key
     *
     * @param serverKey serverKey
     * @return 前端展示key
     */
    public static String getShowKey(String serverKey) {
        for (TaskInfoKey taskInfoKey : values()) {
            if (taskInfoKey.serverKey.equals(serverKey)) {
                return taskInfoKey.showKey;
            }
        }
        return "";
    }

    /**
     * 根据前端展示key获取server的key
     *
     * @param showKey 前端展示key
     * @return server的key
     */
    public static String getServerKey(String showKey) {
        for (TaskInfoKey taskInfoKey : values()) {
            if (taskInfoKey.showKey.equals(showKey)) {
                return taskInfoKey.serverKey;
            }
        }
        return "";
    }
}
