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
 * call exception
 *
 * @author zhouss
 * @since 2022-03-15
 */
public class InvokerException extends RuntimeException {
    private static final long serialVersionUID = -7001064555588186407L;

    /**
     * call exception
     *
     * @param cause exception stack information
     */
    public InvokerException(Throwable cause) {
        super(cause);
    }
}
