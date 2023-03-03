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

package com.huawei.sermant.backend.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 事件原数据
 *
 * @since 2023-03-02
 * @author xuezechao
 */
@Getter
@Setter
public class AgentInstanceMeta {

    /**
     * 实例原数据哈希
     */
    private String metaHash;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 应用
     */
    private String application;

    /**
     * 节点
     */
    private AgentNodeEntity node;

    /**
     * 集群
     */
    private String cluster;

    /**
     * 环境
     */
    private String environment;

    /**
     * 可用区
     */
    private String az;
}
