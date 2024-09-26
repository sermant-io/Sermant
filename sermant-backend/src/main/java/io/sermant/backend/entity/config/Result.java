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
import lombok.Setter;

/**
 * Result information
 *
 * @param <R> Result data type
 * @author zhp
 * @since 2024-05-16
 */
@Getter
@Setter
public class Result<R> {
    /**
     * Result Code, the corresponding enumeration is ResultCodeType
     */
    private String code;

    /**
     * Result message
     */
    private String message;

    /**
     * Result data
     */
    private R data;

    /**
     * Constructor
     *
     * @param code result code
     * @param message result message
     */
    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Constructor
     *
     * @param resultCodeType Result information
     */
    public Result(ResultCodeType resultCodeType) {
        this.code = resultCodeType.getCode();
        this.message = resultCodeType.getMessage();
    }

    /**
     * Constructor
     *
     * @param resultCodeType Result information
     * @param data result data
     */
    public Result(ResultCodeType resultCodeType, R data) {
        this.code = resultCodeType.getCode();
        this.message = resultCodeType.getMessage();
        this.data = data;
    }

    /**
     * Constructor
     *
     * @param code result code
     * @param message result message
     * @param data result data
     */
    public Result(String code, String message, R data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Is the result successful
     *
     * @return true：success false: fail
     */
    public boolean isSuccess() {
        return ResultCodeType.SUCCESS.getCode().equals(this.code);
    }
}
