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

package io.sermant.core.notification;

/**
 * Notification Information
 *
 * @author zhp
 * @since 2023-06-16
 */
public class NotificationInfo {
    /**
     * Notification Type
     */
    private NotificationType notificationType;

    /**
     * Notification Content
     */
    private Object content;

    /**
     * constructor
     *
     * @param notificationType notification type
     * @param content content
     */
    public NotificationInfo(NotificationType notificationType, Object content) {
        this.notificationType = notificationType;
        this.content = content;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
