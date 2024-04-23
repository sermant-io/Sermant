/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.common.exception;

/**
 * Call wrapping exception, which wraps the actual exception of the actual method call
 *
 * @author zhouss
 * @since 2022-03-15
 */
public class InvokerWrapperException extends RuntimeException {
    private static final long serialVersionUID = 4590564899065412250L;

    /**
     * the real method throws an exception
     */
    private final Throwable realException;

    /**
     * true exception packaging
     *
     * @param realException real exception
     */
    public InvokerWrapperException(Throwable realException) {
        this.realException = realException;
    }

    public Throwable getRealException() {
        return realException;
    }
}
