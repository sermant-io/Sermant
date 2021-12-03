/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.controller.agent.constant;

/**
 * 功能描述：agent信息返回给前端的key
 *
 * @author z30009938
 * @since 2021-10-20
 */
public class ResponseColumn {
    public static final String AGENT_ID = "agent_id";
    public static final String AGENT_NAME = "agent_name";
    public static final String AGENT_IP = "domain";
    public static final String AGENT_PORT = "port";
    public static final String AGENT_REGION = "region";
    public static final String AGENT_STATE = "status";
    public static final String AGENT_VERSION = "version";
    public static final String AGENT_APPROVED = "licensed";
    public static final String RESPONSE_DATA_ELEMENT = "data";
    public static final String RESPONSE_TOTAL_ELEMENT = "total";
    public static final String AGENT_LABEL = "status_label";

    /**
     * 根据传入的数据库列名称获取在响应中该列信息的列名称
     *
     * @param databaseColumnName 数据库列名称，因为前段定义的列名称和数据库的不一致，所以这里转换了一下
     * @return 响应列名称信息
     */
    public static String getResponseColumnName(String databaseColumnName) {
        switch (databaseColumnName) {
            case DatabaseColumn.AGENT_ID:
                return AGENT_ID;
            case DatabaseColumn.AGENT_IP:
                return AGENT_IP;
            case DatabaseColumn.AGENT_APPROVED:
                return AGENT_APPROVED;
            case DatabaseColumn.AGENT_NAME:
                return AGENT_NAME;
            case DatabaseColumn.AGENT_PORT:
                return AGENT_PORT;
            case DatabaseColumn.AGENT_STATE:
                return AGENT_STATE;
            case DatabaseColumn.AGENT_REGION:
                return AGENT_REGION;
            case DatabaseColumn.AGENT_VERSION:
                return AGENT_VERSION;
            default:
                return databaseColumnName;
        }
    }
}
