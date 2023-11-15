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

package com.huaweicloud.sermant.backend.server;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.dao.DatabaseType;
import com.huaweicloud.sermant.backend.dao.EventDao;
import com.huaweicloud.sermant.backend.dao.memory.MemoryClientImpl;
import com.huaweicloud.sermant.backend.dao.redis.EventDaoForRedis;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.event.Event;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * 事件服务
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventServer.class);

    private static EventServer eventServer;

    @Autowired
    private BackendConfig backendConfig;

    private EventDao daoService;

    private EventServer() {
    }

    /**
     * 初始化service
     */
    @PostConstruct
    public void init() {
        eventServer = this;
        if (Objects.requireNonNull(backendConfig.getDatabase()) == DatabaseType.REDIS) {
            eventServer.daoService = new EventDaoForRedis(backendConfig);
        } else {
            eventServer.daoService = new MemoryClientImpl(backendConfig);
        }
    }

    /**
     * 增加事件
     *
     * @param event 事件
     * @return true/falses
     */
    public boolean addEvent(Event event) {
        return daoService.addEvent(event);
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
     * 事件查询
     *
     * @param eventsRequestEntity 查询条件
     * @return 查询结果
     */
    public List<QueryResultEventInfoEntity> queryEvent(EventsRequestEntity eventsRequestEntity) {
        return daoService.queryEvent(eventsRequestEntity);
    }

    /**
     * 查询某一页数据
     *
     * @param sessionId 会话id
     * @param page 页书
     * @return 查询结果
     */
    public List<QueryResultEventInfoEntity> queryEventPage(String sessionId, int page) {
        return daoService.queryEventPage(sessionId, page);
    }

    /**
     * 获取查询结果大小
     *
     * @param eventsRequestEntity 查询条件
     * @return 查询结果数量以及不同类型事件数量
     */
    public QueryCacheSizeEntity getQueryCacheSize(EventsRequestEntity eventsRequestEntity) {
        return daoService.getQueryCacheSize(eventsRequestEntity);
    }

    /**
     * 获取webhook 通知事件
     *
     * @param event 上报事件
     * @return webhook 通知事件
     */
    public QueryResultEventInfoEntity getDoNotifyEvent(Event event) {
        return daoService.getDoNotifyEvent(event);
    }
}
