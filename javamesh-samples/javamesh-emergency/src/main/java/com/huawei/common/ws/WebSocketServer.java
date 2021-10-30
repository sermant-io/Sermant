/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.ws;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * websocket管理类
 *
 * @since 2021-10-30
 */
@Slf4j
@ServerEndpoint("/ws")
@Component
public class WebSocketServer {
    private static final CopyOnWriteArraySet<WebSocketServer> WEB_SOCKET_SET = new CopyOnWriteArraySet<>();

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        WEB_SOCKET_SET.add(this);
        log.info("new session add. ");
    }

    @OnClose
    public void onClose() {
        WEB_SOCKET_SET.remove(this);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("Exception occurs. Exception info:{}", error.getMessage());
    }

    public static void sendMessage(String message) {
        for (WebSocketServer item : WEB_SOCKET_SET) {
            try {
                log.info("send msg");
                item.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("Exception occurs. Exception info:{}", e.getMessage());
            }
        }
    }
}
