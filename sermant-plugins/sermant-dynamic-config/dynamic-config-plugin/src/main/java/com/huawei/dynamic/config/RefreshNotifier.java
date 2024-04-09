/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * configure the refresh notifier
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class RefreshNotifier {
    private static final int LISTENER_INIT_SIZE = 4;

    private final List<DynamicConfigListener> dynamicConfigListeners = new ArrayList<>(LISTENER_INIT_SIZE);

    /**
     * add listening data
     *
     * @param listener listener
     */
    public void addListener(DynamicConfigListener listener) {
        dynamicConfigListeners.add(listener);
        dynamicConfigListeners.sort(Comparator.comparingInt(DynamicConfigListener::getOrder));
    }

    /**
     * notification event
     *
     * @param event notification event
     */
    public void refresh(DynamicConfigEvent event) {
        for (DynamicConfigListener listener : dynamicConfigListeners) {
            listener.configChange(event);
        }
    }
}
