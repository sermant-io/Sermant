/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.backend.entity.visibility;

/**
 * Operation type
 *
 * @author zhp
 * @since 2022-12-07
 */
public enum OperateType {
    /**
     * Add
     */
    ADD("ADD"),

    /**
     * Full update consanguinity
     */
    UPDATE_CONSANGUINITY("UPDATE_CONSANGUINITY"),

    /**
     * Full update contract
     */
    UPDATE_CONTRACT("UPDATE_CONTRACT"),

    /**
     * Delete
     */
    DELETE("DELETE"),

    /**
     * Service offline
     */
    OFFLINE("OFFLINE");

    private final String type;

    OperateType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
