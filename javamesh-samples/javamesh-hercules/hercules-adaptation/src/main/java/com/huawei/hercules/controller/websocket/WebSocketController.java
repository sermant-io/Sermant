/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.websocket;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：WebSocket控制相关接口
 *
 * @author z30009938
 * @since 2021-11-03
 */
@RestController
@RequestMapping("/api")
public class WebSocketController {
    /**
     * 日志接口
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private List<WebSocketEventHandler> webSocketEventHandlers;

    /**
     * 通知websocket事件处理接口处理task事件
     *
     * @param message 事件消息
     * @return success成功
     */
    @RequestMapping(value = "/task/ws", method = RequestMethod.POST)
    public Map<String, Object> notifyWebSocket(@RequestBody Map<String, Object> message) {
        String messageJson = JSON.toJSONString(message);
        LOGGER.info("Receive message:{}", message);
        for (WebSocketEventHandler webSocketEventHandler : webSocketEventHandlers) {
            webSocketEventHandler.handleEvent(messageJson);
        }
        message.put("result", "success");
        return message;
    }
}
