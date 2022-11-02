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

package com.huawei.registry.service.config;

import org.springframework.context.ApplicationEvent;

/**
 * 心跳事件
 *
 * @author chengyouling
 * @since 2022-10-20
 */
public class HeartbeatEvent extends ApplicationEvent {
    private final Object state;

    /**
     * 构造方法
     *
     * @param source
     * @param state
     */
    public HeartbeatEvent(Object source, Object state) {
        super(source);
        this.state = state;
    }

    public Object getValue() {
        return this.state;
    }
}
