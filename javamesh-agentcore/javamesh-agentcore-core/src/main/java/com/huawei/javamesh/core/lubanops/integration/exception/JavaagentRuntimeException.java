/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.javamesh.core.lubanops.integration.exception;

/**
 * 所有的lubanops平台的 <br>
 *
 * @author
 * @since 2020年2月29日
 */
public class JavaagentRuntimeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -161240411185863675L;

    public JavaagentRuntimeException() {
    }

    public JavaagentRuntimeException(String message) {
        super(message);
    }

    public JavaagentRuntimeException(String message, Throwable ex) {
        super(message, ex);
    }

    public JavaagentRuntimeException(Throwable ex) {
        super(ex);
    }
}
