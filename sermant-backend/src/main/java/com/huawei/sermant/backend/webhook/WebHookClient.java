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

package com.huawei.sermant.backend.webhook;

import com.huawei.sermant.backend.entity.EventEntity;

import java.util.List;

/**
 * webhook客户端接口定义
 *
 * @since 2023-03-02
 * @author xuezechao
 */
public interface WebHookClient {

    /**
     * 推送事件
     * @param eventEntities 事件信息
     * @return 推送成功或失败
     */
    boolean doNotify(List<EventEntity> eventEntities);

    /**
     * 获取webhook 配置
     * @return 配置
     */
    WebHookConfig getConfig();
}
