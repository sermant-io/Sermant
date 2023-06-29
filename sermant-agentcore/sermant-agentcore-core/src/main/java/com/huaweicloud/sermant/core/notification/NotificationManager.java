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

package com.huaweicloud.sermant.core.notification;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.notification.config.NotificationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 通知管理器
 *
 * @author zhp
 * @since 2023-06-16
 */
public class NotificationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, List<NotificationListener>> NOTIFICATION_LISTENER_MAP = new ConcurrentHashMap<>();

    private static NotificationConfig notificationConfig = ConfigManager.getConfig(NotificationConfig.class);

    private NotificationManager() {
    }

    /**
     * 通知监听器注册。监听该场景下的所有通知
     *
     * @param notificationListener 通知监听器
     * @param typeClass 通知类型
     */
    public static void registry(NotificationListener notificationListener,
                                Class<? extends NotificationType> typeClass) {
        if (!isEnable()) {
            return;
        }
        List<NotificationListener> listenerList = NOTIFICATION_LISTENER_MAP.computeIfAbsent(
                typeClass.getCanonicalName(), s -> new ArrayList<>());
        listenerList.add(notificationListener);
    }

    /**
     * 通知监听器取消注册。监听该场景下的所有通知
     *
     * @param notificationListener 通知监听器
     * @param typeClass 通知类型
     */
    public static void unRegistry(NotificationListener notificationListener,
                                  Class<? extends NotificationType> typeClass) {
        if (!isEnable()) {
            return;
        }
        List<NotificationListener> listenerList = NOTIFICATION_LISTENER_MAP.get(typeClass.getCanonicalName());
        if (listenerList != null) {
            listenerList.remove(notificationListener);
        }
    }

    /**
     * 事件通知
     *
     * @param notificationInfo 通知信息
     */
    public static void doNotify(NotificationInfo notificationInfo) {
        if (!isEnable()) {
            return;
        }
        if (notificationInfo == null || notificationInfo.getNotificationType() == null) {
            LOGGER.fine("notificationInfo is null or notificationType is null");
            return;
        }
        List<NotificationListener> notificationListeners =
                NOTIFICATION_LISTENER_MAP.get(notificationInfo.getNotificationType().getClass().getCanonicalName());
        if (notificationListeners == null || notificationListeners.isEmpty()) {
            return;
        }
        notificationListeners.forEach(notificationListener ->
                CompletableFuture.runAsync(() -> notificationListener.process(notificationInfo)));
    }

    /**
     * 通知开关是否开启
     *
     * @return 通知开关状态
     */
    public static boolean isEnable() {
        if (notificationConfig != null && notificationConfig.isEnable()) {
            return true;
        }
        LOGGER.fine("the notification switch is not turned on");
        return false;
    }
}
