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
     * 飞书webhook名称
     */
    public static final String FEISHU_WEBHOOK_NAME = "Feishu";

    /**
     * welink WEBHOOK名称
     */
    public static final String WELINK_WEBHOOK_NAME = "Welink";

    /**
     * redis 实例原数据key
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
    public static final String FULL_MATCH_KEY = "*";

    /**
     * 前端页面事件每页展示默认数量
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private CommonConst() {

    }
}
