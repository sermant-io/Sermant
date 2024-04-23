/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.injection.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 系统消息监听器
 *
 * @since 2022-07-29
 */
@Component
public class AvailabilityListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityListener.class);

    /**
     * 监控系统消息
     * AvailabilityChangeEvent 类型的消息都会触发此方法被回调
     *
     * @param event 事件
     */
    @EventListener
    public void onStateChange(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        LOGGER.info("{} : {}", event.getState().getClass().getSimpleName(), event.getState());
    }
}
