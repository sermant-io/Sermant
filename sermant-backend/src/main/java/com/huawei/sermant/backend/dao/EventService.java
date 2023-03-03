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

import com.huawei.sermant.backend.common.conf.EventConfig;
import com.huawei.sermant.backend.dao.redis.EventDaoForRedis;
import com.huawei.sermant.backend.entity.AgentInstanceMeta;
import com.huawei.sermant.backend.entity.EventEntity;
import com.huawei.sermant.backend.entity.EventsRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 事件服务
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventConfig eventConfig;
    private EventDao daoService;
    private static EventService eventService = new EventService();

    private EventService() {
    }

    @PostConstruct
    public void init() {
        eventService = this;
        switch (eventConfig.getDatabase()) {
            default:
                this.daoService = new EventDaoForRedis(eventConfig);
        }
    }

    public static EventService getInstance() {
        return eventService;
    }

    public boolean addEvent(EventEntity eventEntity) {
        return daoService.addEvent(eventEntity);
    }

    public boolean addEvent(AgentInstanceMeta agentInstanceMeta) {
        return daoService.addInstanceMeta(agentInstanceMeta);
    }

    public boolean delEvent(EventEntity eventEntity) {
        return daoService.deleteEvent(eventEntity);
    }

    public boolean delEvent(AgentInstanceMeta agentInstanceMeta) {
        return daoService.deleteInstanceMeta(agentInstanceMeta);
    }

    public List<EventEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        return daoService.queryEvent(eventsRequestEntity);
    }
}
