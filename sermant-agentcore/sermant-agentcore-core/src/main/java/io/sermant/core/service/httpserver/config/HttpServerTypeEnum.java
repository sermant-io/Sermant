/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.service.httpserver.config;

/**
 * HTTP Server Type Enumeration
 *
 * @author zwmagic
 * @since 2024-02-04
 */
public enum HttpServerTypeEnum {
    /**
     * Simple server type, utilizing JDK's built-in Http Server
     */
    SIMPLE("simple");

    /**
     * Type identifier
     */
    private final String type;

    /**
     * Enum constructor
     *
     * @param type String identifier for the enum type
     */
    HttpServerTypeEnum(String type) {
        this.type = type;
    }

    /**
     * Retrieves the string identifier of the enum type
     *
     * @return Identifier string of the type
     */
    public String getType() {
        return type;
    }
}
