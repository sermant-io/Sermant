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

package com.huaweicloud.sermant.backend.entity.event;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件查询请求实体
 *
 * @since 2023-03-02
 * @author xuezechao
 */
@Getter
@Setter
public class EventsRequestEntity {

    /**
     * 服务名
     */
    private List<String> service = new ArrayList<>();

    /**
     * 地址
     */
    private List<String> ip = new ArrayList<>();

    /**
     * 范围
     */
    private List<String> scope = new ArrayList<>();

    /**
     * 类型
     */
    private List<String> type = new ArrayList<>();

    /**
     * 级别
     */
    private List<String> level = new ArrayList<>();

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 截止时间
     */
    private long endTime;

    /**
     * session id
     */
    private String sessionId;
}
