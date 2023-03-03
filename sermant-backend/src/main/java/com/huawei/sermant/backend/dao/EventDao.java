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

package com.huawei.sermant.backend.dao;

import com.huawei.sermant.backend.entity.AgentInstanceMeta;
import com.huawei.sermant.backend.entity.EventEntity;
import com.huawei.sermant.backend.entity.EventsRequestEntity;

import java.util.List;

/**
 * 数据库接口
 *
 * @author xuezechao
 * @since 2023-03-02
 */
public interface EventDao {
    boolean addEvent(EventEntity eventEntity);

    boolean addInstanceMeta(AgentInstanceMeta agentInstanceMeta);

    boolean deleteEvent(EventEntity eventEntity);

    boolean deleteInstanceMeta(AgentInstanceMeta agentInstanceMeta);

    List<EventEntity> queryEvent(EventsRequestEntity eventsRequestEntity);
}
