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

package com.huawei.javamesh.core.lubanops.core.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.huawei.javamesh.core.lubanops.bootstrap.api.EventDispatcher;
import com.huawei.javamesh.core.lubanops.bootstrap.event.ApmEvent;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.core.executor.ExecuteRepository;

/**
 * @author
 * @date 2021/2/3 15:34
 */
public class ApmEventDispatcher implements EventDispatcher {
    private static final Logger LOGGER = LogFactory.getLogger();

    EventBus eventBus;

    AsyncEventBus asyncEventBus;

    @Inject
    public ApmEventDispatcher(ExecuteRepository executeRepository) {
        eventBus = new EventBus();
        asyncEventBus = new AsyncEventBus(executeRepository.getSharedExecutor());
    }

    @Override
    public void dispatch(ApmEvent event) {

        if (event == null || event.getEventType() == null) {
            LOGGER.log(Level.SEVERE, "[EVENT DISPATCHER]empty event object.");
        }
        eventBus.post(event);
    }
}
