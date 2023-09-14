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

package com.huaweicloud.sermant.core.common;

/**
 * 描述agent类型
 *
 * @author luanwenfei
 * @since 2023-09-09
 */
public enum AgentType {
    /**
     * premain方式启动的agent
     */
    PREMAIN(0),
    /**
     * agentmain方式启动的agent
     */
    AGENTMAIN(1);

    private final int value;

    AgentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
