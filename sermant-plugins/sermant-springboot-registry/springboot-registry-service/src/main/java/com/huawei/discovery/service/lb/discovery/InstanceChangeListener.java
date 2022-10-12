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

package com.huawei.discovery.service.lb.discovery;

import com.huawei.discovery.entity.ServiceInstance;

/**
 * 实例变更监听器, 当实例发生变更, 则会通知
 *
 * @author zhouss
 * @since 2022-10-12
 */
public interface InstanceChangeListener {
    /**
     * 通知
     *
     * @param eventType 事件类型
     * @param serviceInstance 变更实例
     */
    void notify(EventType eventType, ServiceInstance serviceInstance);

    /**
     * 事件类型
     *
     * @since 2022-10-12
     */
    enum EventType {
        /**
         * 新增
         */
        ADDED,

        /**
         * 更新
         */
        UPDATED,

        /**
         * 删除
         */
        DELETED
    }
}
