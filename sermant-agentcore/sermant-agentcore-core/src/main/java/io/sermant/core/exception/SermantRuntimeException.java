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

package io.sermant.core.exception;

/**
 * Sermant Runtime Exception
 *
 * @author zhp
 * @since 2024-06-27
 */
public class SermantRuntimeException extends RuntimeException {
    /**
     * Constructor
     *
     * @param message message
     */
    public SermantRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause The cause of the exception
     */
    public SermantRuntimeException(Throwable cause) {
        super(cause);
    }
}
