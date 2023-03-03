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

package com.huawei.sermant.backend.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 事件信息
 *
 * @since 2023-03-02
 * @author xuezechao
 */
@Getter
@Setter
public class EventEntity {
    /**
     * 实例元数据
     */
    private String meta;

    /**
     * 事件等级
     */
    private EventLevel level;

    /**
     * 事件类型
     */
    private EventType type;

    /**
     * 触发时间
     */
    private long time;

    /**
     * 事件区域
     */
    private String scope;

    /**
     * 事件信息
     */
    private EventMessageEntity info;
}
