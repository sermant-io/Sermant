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

package io.sermant.backend.handler;

import io.sermant.backend.common.conf.BackendConfig;
import io.sermant.backend.entity.event.Event;
import io.sermant.backend.entity.event.EventInfo;
import io.sermant.backend.entity.event.EventLevel;
import io.sermant.backend.entity.event.EventType;
import io.sermant.backend.entity.event.QueryResultEventInfoEntity;
import io.sermant.backend.server.EventServer;
import io.sermant.backend.webhook.WebHookClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

/**
 * Event Push Handler
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Component
public class EventPushHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPushHandler.class);

    private List<WebHookClient> webHookClients = new ArrayList<>();

    @Autowired
    private EventServer eventServer;

    @Autowired
    private BackendConfig backendConfig;

    /**
     * Constructor
     */
    public EventPushHandler() {
        this.init();
    }

    /**
     * Initialize the webhook
     */
    private void init() {
        ServiceLoader<WebHookClient> loader = ServiceLoader.load(WebHookClient.class);
        for (WebHookClient webHookClient : loader) {
            webHookClients.add(webHookClient);
        }
    }

    /**
     * Push event
     *
     * @param eventList event information
     */
    public void pushEvent(List<Event> eventList) {
        if (eventList.size() <= 0) {
            LOGGER.error("push event failed, the size of event message is: 0");
            return;
        }

        List<QueryResultEventInfoEntity> needPushWebHook = writeEventAndGetPushWebHookEvent(eventList);

        if (needPushWebHook.size() <= 0) {
            return;
        }

        for (WebHookClient webHookClient : webHookClients) {
            if (webHookClient.getConfig().getEnable()) {
                if (!webHookClient.doNotify(needPushWebHook)) {
                    // push failed
                    LOGGER.error(String.format(Locale.ROOT, "push event to webhook:{%s} failed!",
                            webHookClient.getConfig().getName()));
                }
            }
        }
    }

    private List<QueryResultEventInfoEntity> writeEventAndGetPushWebHookEvent(List<Event> eventList) {
        List<QueryResultEventInfoEntity> events = new ArrayList<>();
        for (Event event : eventList) {
            eventServer.addEvent(event);
            if (event.getEventLevel().getLevelThreshold() >= EventLevel.valueOf(
                    backendConfig.getWebhookPushEventThreshold().toUpperCase(Locale.ROOT)).getLevelThreshold()) {
                events.add(eventServer.getDoNotifyEvent(event));
            }
        }
        return events;
    }

    /**
     * Get the webhook client
     *
     * @return webhook client
     */
    public List<WebHookClient> getWebHookClients() {
        return webHookClients;
    }

    /**
     * webhook test
     *
     * @param webhookId webhook id
     */
    public void testWebhook(int webhookId) {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setName("test");
        eventInfo.setDescription("test webhook");
        QueryResultEventInfoEntity queryResultEventInfoEntity = new QueryResultEventInfoEntity();
        HashMap<String, String> meta = new HashMap<>();
        meta.put("service", "service");
        meta.put("ip", "127.0.0.1");
        queryResultEventInfoEntity.setMeta(meta);
        queryResultEventInfoEntity.setTime(new Date().getTime());
        queryResultEventInfoEntity.setScope("framework");
        queryResultEventInfoEntity.setLevel(EventLevel.EMERGENCY.name());
        queryResultEventInfoEntity.setType(EventType.GOVERNANCE.getDescription());
        queryResultEventInfoEntity.setInfo(eventInfo);
        List<QueryResultEventInfoEntity> testEvents = new ArrayList<>();
        testEvents.add(queryResultEventInfoEntity);
        testEvents.add(queryResultEventInfoEntity);
        for (WebHookClient webHookClient : webHookClients) {
            if (webhookId == webHookClient.getConfig().getId()) {
                webHookClient.doNotify(testEvents);
            }
        }
    }
}
