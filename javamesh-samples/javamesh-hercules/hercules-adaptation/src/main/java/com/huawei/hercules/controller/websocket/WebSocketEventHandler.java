/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.websocket;

/**
 * 功能描述：WebSocket接口处理者
 *
 * @author z30009938
 * @since 2021-11-03
 */
public interface WebSocketEventHandler {
    /**
     * 处理websocket事件消息
     *
     * @param message 消息
     */
    void handleEvent(String message);
}
