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

package com.huawei.flowcontrol.common.exception;

/**
 * 调用包装异常，包装实际方法调用的真正异常
 *
 * @author zhouss
 * @since 2022-03-15
 */
public class InvokerWrapperException extends RuntimeException {
    private static final long serialVersionUID = 4590564899065412250L;

    /**
     * 真实方法抛出异常
     */
    private final Throwable realException;

    /**
     * 真实异常包装
     *
     * @param realException 真实异常
     */
    public InvokerWrapperException(Throwable realException) {
        this.realException = realException;
    }

    public Throwable getRealException() {
        return realException;
    }
}
