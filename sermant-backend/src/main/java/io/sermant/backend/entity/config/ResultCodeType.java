/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.entity.config;

import lombok.Getter;

/**
 * Result encoding type
 *
 * @author zhp
 * @since 2024-05-16
 */
@Getter
public enum ResultCodeType {
    /**
     * Interface call successful
     */
    SUCCESS("00", "Success."),

    /**
     * Unable to establish connection with Configuration Center
     */
    CONNECT_FAIL("01", "Unable to establish connection with Configuration Center."),

    /**
     * Configuration query failed
     */
    QUERY_FAIL("02", "Configuration query failed."),

    /**
     * configuration item already exists
     */
    EXISTS("03", "configuration item already exists."),

    /**
     * Failed to add configuration
     */
    ADD_FAIL("04", "Failed to add configuration."),

    /**
     * Failed to modify configuration
     */
    MODIFY_FAIL("05", "Failed to modify configuration."),

    /**
     * Failed to delete configuration
     */
    DELETE_FAIL("06", "Failed to delete configuration."),

    /**
     * Configuration does not exist
     */
    NOT_EXISTS("07", "Configuration does not exist."),

    /**
     * Missing parameter information
     */
    MISS_PARAM("08", "Missing parameter information."),

    /**
     * Interface call failed
     */
    FAIL("09", "Failure.");

    private final String code;

    private final String message;

    ResultCodeType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
