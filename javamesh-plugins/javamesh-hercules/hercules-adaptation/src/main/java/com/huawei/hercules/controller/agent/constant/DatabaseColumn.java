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

import org.springframework.util.StringUtils;

/**
 * 功能描述：agent信息在数据库中的key值
 *
 * @author z30009938
 * @since 2021-10-20
 */
public class DatabaseColumn {
    public static final String AGENT_ID = "id";
    public static final String AGENT_NAME = "hostName";
    public static final String AGENT_IP = "ip";
    public static final String AGENT_PORT = "port";
    public static final String AGENT_REGION = "region";
    public static final String AGENT_STATE = "state";
    public static final String AGENT_VERSION = "version";
    public static final String AGENT_APPROVED = "approved";
    public static final String RESPONSE_DATA_ELEMENT = "data";
    public static final String RESPONSE_TOTAL_ELEMENT = "total";

    /**
     * 根据传入的交互列名称获取在数据库中该列信息的列名称
     *
     * @param responseColumnName 响应列名称，因为前段定义的列名称和数据库的不一致，所以这里转换了一下
     * @return 数据库列名称信息
     */
    public static String getDatabaseColumnName(String responseColumnName) {
        if (StringUtils.isEmpty(responseColumnName)) {
            return responseColumnName;
        }
        switch (responseColumnName) {
            case ResponseColumn.AGENT_ID:
                return AGENT_ID;
            case ResponseColumn.AGENT_IP:
                return AGENT_IP;
            case ResponseColumn.AGENT_APPROVED:
                return AGENT_APPROVED;
            case ResponseColumn.AGENT_NAME:
                return AGENT_NAME;
            case ResponseColumn.AGENT_PORT:
                return AGENT_PORT;
            case ResponseColumn.AGENT_LABEL:
            case ResponseColumn.AGENT_STATE:
                return AGENT_STATE;
            case ResponseColumn.AGENT_REGION:
                return AGENT_REGION;
            case ResponseColumn.AGENT_VERSION:
                return AGENT_VERSION;
            default:
                return responseColumnName;
        }
    }
}
