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

package com.huaweicloud.sermant.backend.controller;

import com.huaweicloud.sermant.backend.common.conf.BackendConfig;
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.entity.event.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.EventsResponseEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryCacheSizeEntity;
import com.huaweicloud.sermant.backend.entity.event.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.entity.event.WebhooksIdRequestEntity;
import com.huaweicloud.sermant.backend.entity.event.WebhooksResponseEntity;
import com.huaweicloud.sermant.backend.handler.EventPushHandler;
import com.huaweicloud.sermant.backend.server.EventServer;
import com.huaweicloud.sermant.backend.webhook.WebHookClient;
import com.huaweicloud.sermant.backend.webhook.WebHookConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 事件信息 Controller
 *
 * @author xuezechao
 * @since 2023-03-13
 */
@Component
@RestController
@RequestMapping("/sermant")
public class EventController {
    @Autowired
    private BackendConfig backendConfig;

    @Autowired
    private EventServer eventServer;

    @Autowired
    private EventPushHandler eventPushHandler;

    /**
     * 查询某页数据
     *
     * @param request 请求
     * @param page 页码
     * @return 查询结果
     */
    @GetMapping("/event/events/page")
    public HashMap<String, List<QueryResultEventInfoEntity>> queryEventPage(
            HttpServletRequest request,
            @RequestParam(value = "page") int page) {
        HttpSession session = request.getSession();
        HashMap<String, List<QueryResultEventInfoEntity>> result = new HashMap<>();
        result.put("events", eventServer.queryEventPage(session.getId(), page));
        return result;
    }

    /**
     * 事件查询
     *
     * @param request 请求
     * @param eventsRequestEntity 请求参数
     * @return 查询结果
     */
    @GetMapping("/event/events")
    public EventsResponseEntity queryEvent(
            HttpServletRequest request, @ModelAttribute EventsRequestEntity eventsRequestEntity) {
        HttpSession session = request.getSession();
        eventsRequestEntity.setSessionId(session.getId());
        EventsResponseEntity eventsResponseEntity = new EventsResponseEntity();
        List<QueryResultEventInfoEntity> queryResult = eventServer.queryEvent(eventsRequestEntity);
        setQuerySize(eventsResponseEntity, eventServer.getQueryCacheSize(eventsRequestEntity));
        eventsResponseEntity.setEvents(queryResult);
        return eventsResponseEntity;
    }

    /**
     * 查询webhook
     *
     * @return webhook信息
     */
    @GetMapping("/event/webhooks")
    public WebhooksResponseEntity getWebhooks() {
        WebhooksResponseEntity webhooksResponseEntity = new WebhooksResponseEntity();
        List<WebHookClient> webHookClients = eventPushHandler.getWebHookClients();
        webhooksResponseEntity.setTotal(webHookClients.size());
        List<WebHookConfig> webHookConfigs = new ArrayList<>();
        for (WebHookClient webHookClient : webHookClients) {
            webHookConfigs.add(webHookClient.getConfig());
        }
        webhooksResponseEntity.setWebhooks(webHookConfigs);
        return webhooksResponseEntity;
    }

    /**
     * 配置webhook
     *
     * @param webhooksIdRequestEntity webhook配置请求实体
     * @param id webhook id
     * @return 配置结果
     */
    @PutMapping("/event/webhooks/{id}")
    public boolean setWebhook(@RequestBody(required = false) WebhooksIdRequestEntity webhooksIdRequestEntity,
            @PathVariable String id) {
        List<WebHookClient> webHookClients = eventPushHandler.getWebHookClients();
        for (WebHookClient webHookClient : webHookClients) {
            WebHookConfig config = webHookClient.getConfig();
            if (id.equals(String.valueOf(config.getId()))) {
                config.setUrl(webhooksIdRequestEntity.getUrl());
                config.setEnable(webhooksIdRequestEntity.isEnable());
            }
        }
        return true;
    }

    /**
     * webhook 测试接口
     *
     * @param param webhook id
     */
    @PostMapping("/event/webhooks/test")
    public void testWebhook(@RequestBody Map<String, Object> param) {
        eventPushHandler.testWebhook((Integer) param.get("id"));
    }

    /**
     * 设置查询事件数量信息
     *
     * @param eventsResponseEntity 事件查询结果
     * @param queryCacheSize 查询缓存
     */
    private void setQuerySize(EventsResponseEntity eventsResponseEntity, QueryCacheSizeEntity queryCacheSize) {
        HashMap<String, Integer> eventCount = new HashMap<>();
        eventCount.put("emergency", queryCacheSize.getEmergencyNum());
        eventCount.put("important", queryCacheSize.getImportantNum());
        eventCount.put("normal", queryCacheSize.getNormalNum());
        eventsResponseEntity.setEventCount(eventCount);
        int totalPage = queryCacheSize.getTotal() / CommonConst.DEFAULT_PAGE_SIZE;
        if (0 < queryCacheSize.getTotal() % CommonConst.DEFAULT_PAGE_SIZE) {
            totalPage += 1;
        }
        eventsResponseEntity.setTotalPage(totalPage);
    }
}
