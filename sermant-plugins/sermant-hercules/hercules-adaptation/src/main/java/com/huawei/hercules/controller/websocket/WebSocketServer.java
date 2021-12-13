/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.hercules.controller.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述：监听压测任务状态变化web socket server
 *
 * 
 * @since 2021-11-02
 */
@Component
@ServerEndpoint("/ws")
public class WebSocketServer implements WebSocketEventHandler {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * 每一个请求session和压测任务id的映射map
     */
    private static final Map<String, Session> SESSION_TASK_CACHE = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("Receive connection, session={}", session.getId());

        Session existSession = SESSION_TASK_CACHE.get(session.getId());
        if (existSession != null) {
            return;
        }

        // 如果对应id还没有监听的session，则直接创建一个新的放入map
        SESSION_TASK_CACHE.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        SESSION_TASK_CACHE.remove(session.getId());
    }

    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("Web Socket error!", throwable);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        session.getAsyncRemote().sendText(JSON.toJSONString(response));
    }

    @PreDestroy
    public void destroy() {
        SESSION_TASK_CACHE.clear();
    }

    @Override
    public void handleEvent(String message) {
        LOGGER.info("Start to send message:{}", message);
        JSONObject messageMap = JSONObject.parseObject(message);
        String messageContent = messageMap.getString("message");
        for (Session session : SESSION_TASK_CACHE.values()) {
            LOGGER.debug("Send message to {}, message:{}.", session, message);
            session.getAsyncRemote().sendText(messageContent);
        }
    }
}
