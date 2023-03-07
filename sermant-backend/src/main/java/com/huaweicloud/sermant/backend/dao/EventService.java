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

package com.huaweicloud.sermant.backend.dao;

import com.huaweicloud.sermant.backend.common.conf.EventConfig;
import com.huaweicloud.sermant.backend.dao.redis.EventDaoForRedis;
import com.huaweicloud.sermant.backend.entity.EventInfoEntity;
import com.huaweicloud.sermant.backend.entity.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.PostConstruct;

/**
 * 事件服务
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private static EventService eventService = new EventService();

    @Autowired
    private EventConfig eventConfig;
    private EventDao daoService;

    private EventService() {
    }

    /**
     * 初始化service
     */
    @PostConstruct
    public void init() {
        eventService = this;
        switch (eventConfig.getDatabase()) {
            default:
                this.daoService = new EventDaoForRedis(eventConfig);
        }
    }

    /**
     * 获取service实例
     *
     * @return service
     */
    public static EventService getInstance() {
        return eventService;
    }

    /**
     * 增加事件
     *
     * @param eventInfoEntity 事件
     * @return true/falses
     */
    public boolean addEvent(EventInfoEntity eventInfoEntity) {
        return daoService.addEvent(eventInfoEntity);
    }

    /**
     * 增加agent实例
     *
     * @param instanceMeta agent实例
     * @return true/falses
     */
    public boolean addEvent(InstanceMeta instanceMeta) {
        return daoService.addInstanceMeta(instanceMeta);
    }

    /**
     * 删除事件
     *
     * @param eventInfoEntity 事件
     * @return true/false
     */
    public boolean delEvent(EventInfoEntity eventInfoEntity) {
        return daoService.deleteEvent(eventInfoEntity);
    }

    /**
     * 删除agent实例
     *
     * @param instanceMeta agent实例信息
     * @return true/false
     */
    public boolean delEvent(InstanceMeta instanceMeta) {
        return daoService.deleteInstanceMeta(instanceMeta);
    }

    /**
     * 事件查询
     *
     * @param eventsRequestEntity 查询条件
     * @return 查询结果
     */
    public List<EventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        return daoService.queryEvent(eventsRequestEntity);
    }
}
