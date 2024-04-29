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

package io.sermant.backend.controller;

import io.sermant.backend.common.conf.BackendConfig;
import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.entity.event.EventsRequestEntity;
import io.sermant.backend.entity.event.EventsResponseEntity;
import io.sermant.backend.entity.event.QueryCacheSizeEntity;
import io.sermant.backend.entity.event.QueryResultEventInfoEntity;
import io.sermant.backend.entity.event.WebhooksIdRequestEntity;
import io.sermant.backend.entity.event.WebhooksResponseEntity;
import io.sermant.backend.handler.EventPushHandler;
import io.sermant.backend.server.EventServer;
import io.sermant.backend.webhook.WebHookClient;
import io.sermant.backend.webhook.WebHookConfig;

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
 * Event Controller
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
     * Query data with specific page
     *
     * @param request request
     * @param page page
     * @return query result
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
     * Event query
     *
     * @param request request
     * @param eventsRequestEntity Event query request entity
     * @return query result
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
     * Query webhook
     *
     * @return webhook information
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
     * Configure webhook
     *
     * @param webhooksIdRequestEntity Webhook request entity
     * @param id webhook id
     * @return configure result
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
     * webhook test function
     *
     * @param param webhook id
     */
    @PostMapping("/event/webhooks/test")
    public void testWebhook(@RequestBody Map<String, Object> param) {
        eventPushHandler.testWebhook((Integer) param.get("id"));
    }

    /**
     * Set the number of events to be queried
     *
     * @param eventsResponseEntity Event query response entity
     * @param queryCacheSize Event query cache size
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
