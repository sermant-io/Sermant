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

/**
 * 公共配置常量
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public class CommonConst {

    /**
     * 钉钉webhook名称
     */
    public static final String DINGDING_WEBHOOK_NAME = "DingDing";

    /**
     * 钉钉webhook id
     */
    public static final int DINGDING_WEBHOOK_ID = 1;

    /**
     * 飞书webhook名称
     */
    public static final String FEISHU_WEBHOOK_NAME = "Feishu";

    /**
     * 飞书webhook id
     */
    public static final int FEISHU_WEBHOOK_ID = 0;

    /**
     * welink WEBHOOK名称
     */
    public static final String WELINK_WEBHOOK_NAME = "Welink";

    /**
     * welink WEBHOOK id
     */
    public static final int WELINK_WEBHOOK_ID = 2;

    /**
     * 默认redis地址
     */
    public static final String DEFAULT_REDIS_ADDRESS = "127.0.0.1";

    /**
     * 默认redis端口
     */
    public static final int DEFAULT_REDIS_PORT = 6379;

    /**
     * redis 实例元数据key
     */
    public static final String REDIS_HASH_KEY_OF_INSTANCE_META = "sermant_meta";

    /**
     * redis 事件key
     */
    public static final String REDIS_EVENT_KEY = "sermant_events_hash";

    /**
     * redis 事件field集合的key
     */
    public static final String REDIS_EVENT_FIELD_SET_KEY = "sermant_event_keyset";

    /**
     * redis 全匹配字符*
     */
    public static final String FULL_MATCH_KEY = ".*";

    /**
     * 拼接redis key 字符
     */
    public static final String JOIN_REDIS_KEY = "_";

    /**
     * redis 地址切分字符
     */
    public static final String REDIS_ADDRESS_SPLIT = ":";

    /**
     * 前端页面事件每页展示默认数量
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 钉钉 webhook markdown 事件格式
     */
    public static final String DINGDING_MARKDOWN_EVENT_FORMAT =
            "### level: <font color=\"#dd0000\">%s</font><br />" + System.lineSeparator()
                    + "### service: %s" + System.lineSeparator()
                    + "### ip: %s" + System.lineSeparator()
                    + "### time: %s" + System.lineSeparator()
                    + "### scope: %s" + System.lineSeparator()
                    + "### type: %s" + System.lineSeparator()
                    + "### content:" + System.lineSeparator()
                    + "> name: %s  " + System.lineSeparator()
                    + "> description: %s  " + System.lineSeparator()
                    + "---  " + System.lineSeparator();

    /**
     * 钉钉 webhook markdown 日志格式
     */
    public static final String DINGDING_MARKDOWN_LOG_FORMAT =
            "### level: <font color=\"#dd0000\">%s</font><br />" + System.lineSeparator()
                    + "### service: %s" + System.lineSeparator()
                    + "### ip: %s" + System.lineSeparator()
                    + "### time: %s" + System.lineSeparator()
                    + "### scope: %s" + System.lineSeparator()
                    + "### type: %s" + System.lineSeparator()
                    + "### content:" + System.lineSeparator()
                    + "> logLevel: %s  " + System.lineSeparator()
                    + "> logMessage: %s  " + System.lineSeparator()
                    + "> logClass: %s  " + System.lineSeparator()
                    + "> logMethod: %s  " + System.lineSeparator()
                    + "> logLineNumber: %s  " + System.lineSeparator()
                    + "> logThreadId: %s  " + System.lineSeparator()
                    + "> throwable: %s  " + System.lineSeparator()
                    + "---  " + System.lineSeparator();

    private CommonConst() {

    }
}
