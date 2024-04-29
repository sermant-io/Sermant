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

package io.sermant.integration.entity;

import java.io.Serializable;

/**
 * 泳道测试实体
 *
 * @author provenceee
 * @since 2023-03-02
 */
public class LaneTestEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;

    private final boolean enabled;

    /**
     * 构造方法
     *
     * @param id id
     * @param enabled enabled
     */
    public LaneTestEntity(int id, boolean enabled) {
        this.id = id;
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }
}